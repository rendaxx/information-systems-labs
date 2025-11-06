import { useCallback, useMemo, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServerTable } from '@shared/lib/server-table';
import { DataTable } from '@shared/ui/data-table';
import { Button } from '@shared/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@shared/ui/dialog';
import { useToastHelpers } from '@app/providers/toast-provider';
import { useWebSocketSubscription } from '@app/providers/websocket-provider';
import type { RoutePoint, SaveRoutePoint } from '@rendaxx/api-ts';
import type { IMessage } from '@stomp/stompjs';
import {
  createRoutePoint,
  deleteRoutePoint,
  fetchRoutePoint,
  fetchRoutePoints,
  updateRoutePoint
} from '@entities/route-points/api/route-points-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageMetadata } from '@shared/api/types';
import { parseApiError } from '@shared/api/errors';
import { RoutePointForm } from './route-point-form';

const defaultMeta: PageMetadata = {
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  sort: [],
  first: true,
  last: true,
  empty: true
};

interface EntityChange<T> {
  id: number;
  dto: T | null;
  changeType: 'CREATED' | 'UPDATED' | 'DELETED';
}

export function RoutePointsPage() {
  const queryClient = useQueryClient();
  const { notifyError, notifySuccess } = useToastHelpers();
  const { state, handlers, request } = useServerTable({ size: 20 });

  const [isCreateOpen, setCreateOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingPoint, setEditingPoint] = useState<RoutePoint | null>(null);

  const pointsQuery = useQuery({
    queryKey: queryKeys.routePoints.list(request),
    queryFn: () => fetchRoutePoints(request),
    keepPreviousData: true,
    staleTime: 5_000
  });

  const meta = pointsQuery.data?.meta ?? { ...defaultMeta, page: state.page, size: state.size };
  const routePoints = pointsQuery.data?.data ?? [];

  const columns = useMemo<ColumnDef<RoutePoint>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        enableSorting: true,
        cell: ({ row }) => <span className="font-medium">#{row.original.id}</span>
      },
      {
        id: 'route',
        header: 'Маршрут',
        cell: ({ row }) => {
          const route = row.original.routeId;
          return route ? `Маршрут #${route}` : '—';
        }
      },
      {
        id: 'retailPoint',
        header: 'Точка',
        cell: ({ row }) => row.original.retailPoint?.name ?? `ID ${row.original.retailPoint?.id ?? '—'}`
      },
      {
        accessorKey: 'operationType',
        header: 'Операция'
      },
      {
        id: 'window',
        header: 'Временное окно',
        cell: ({ row }) => (
          <div className="flex flex-col text-sm">
            <span>Начало: {formatDate(row.original.plannedStartTime)}</span>
            <span>Конец: {formatDate(row.original.plannedEndTime)}</span>
          </div>
        )
      },
      {
        accessorKey: 'orderNumber',
        header: 'Порядок',
        enableSorting: true
      },
      {
        id: 'orders',
        header: 'Заказы',
        cell: ({ row }) => {
          const orders = row.original.orders ?? [];
          if (orders.length === 0) {
            return '—';
          }
          return orders
            .map((order) => `#${order.id}`)
            .slice(0, 3)
            .join(', ') + (orders.length > 3 ? '…' : '');
        }
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-2">
            <Button size="sm" variant="secondary" type="button" onClick={() => openEdit(row.original.id)}>
              Редактировать
            </Button>
            <Button size="sm" variant="ghost" type="button" onClick={() => handleDelete(row.original)}>
              Удалить
            </Button>
          </div>
        )
      }
    ],
    []
  );

  const handleApiError = async (error: unknown) => {
    const parsed = await parseApiError(error);
    notifyError('Ошибка', parsed.message);
  };

  const createMutation = useMutation({
    mutationFn: (payload: SaveRoutePoint) => createRoutePoint(payload),
    onSuccess: () => {
      notifySuccess('Точка маршрута создана');
      setCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.routePoints.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SaveRoutePoint }) => updateRoutePoint(id, payload),
    onSuccess: () => {
      notifySuccess('Точка маршрута обновлена');
      closeEdit();
      queryClient.invalidateQueries({ queryKey: queryKeys.routePoints.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteRoutePoint(id),
    onSuccess: () => {
      notifySuccess('Точка маршрута удалена');
      queryClient.invalidateQueries({ queryKey: queryKeys.routePoints.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const handleRoutePointEvent = useCallback(
    (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body) as EntityChange<RoutePoint>;
        if (!payload) {
          return;
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.routePoints.all });
        if (payload.changeType === 'CREATED') {
          notifySuccess('Добавлена новая точка маршрута');
        }
      } catch (error) {
        console.error('Ошибка разбора события точки маршрута', error);
      }
    },
    [notifySuccess, queryClient]
  );

  useWebSocketSubscription('/topic/route-points', handleRoutePointEvent);

  const openEdit = async (id: number) => {
    try {
      setEditingId(id);
      const point = await queryClient.fetchQuery({
        queryKey: queryKeys.routePoints.detail(id),
        queryFn: () => fetchRoutePoint(id)
      });
      setEditingPoint(point);
    } catch (error) {
      const parsed = await parseApiError(error);
      notifyError('Не удалось загрузить точку маршрута', parsed.message);
      closeEdit();
    }
  };

  const handleDelete = (point: RoutePoint) => {
    if (!point.id) return;
    if (window.confirm(`Удалить точку маршрута #${point.id}?`)) {
      deleteMutation.mutate(point.id);
    }
  };

  const closeEdit = () => {
    setEditingId(null);
    setEditingPoint(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-semibold">Точки маршрута</h1>
          <p className="text-muted-foreground">Управляйте последовательностью и окнами посещения торговых точек.</p>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          Добавить точку маршрута
        </Button>
      </div>

      <DataTable
        data={routePoints}
        columns={columns}
        meta={meta}
        state={state}
        handlers={handlers}
        isLoading={pointsQuery.isLoading}
        isFetching={pointsQuery.isFetching && !pointsQuery.isLoading}
        emptyState="Точки маршрута отсутствуют"
      />

      <Dialog open={isCreateOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Новая точка маршрута</DialogTitle>
          </DialogHeader>
          <RoutePointForm
            onSubmit={(payload) => createMutation.mutateAsync(payload)}
            onCancel={() => setCreateOpen(false)}
            isSubmitting={createMutation.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(editingId)} onOpenChange={(open) => !open && closeEdit()}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Редактирование точки маршрута</DialogTitle>
          </DialogHeader>
          {editingId && !editingPoint ? (
            <p className="text-sm text-muted-foreground">Загрузка данных точки…</p>
          ) : null}
          {editingPoint ? (
            <RoutePointForm
              initialRoutePoint={editingPoint}
              onSubmit={(payload) =>
                editingPoint?.id ? updateMutation.mutateAsync({ id: editingPoint.id, payload }) : Promise.resolve()
              }
              onCancel={closeEdit}
              isSubmitting={updateMutation.isPending}
            />
          ) : null}
        </DialogContent>
      </Dialog>
    </div>
  );
}

function formatDate(value?: Date) {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }
  return new Intl.DateTimeFormat('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date);
}
