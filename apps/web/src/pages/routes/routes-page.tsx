import { useCallback, useMemo, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import type { IMessage } from '@stomp/stompjs';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServerTable } from '@shared/lib/server-table';
import { DataTable } from '@shared/ui/data-table';
import { Button } from '@shared/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@shared/ui/dialog';
import { useToastHelpers } from '@app/providers/toast-provider';
import { useWebSocketSubscription } from '@app/providers/websocket-provider';
import type { Route, SaveRoute } from '@rendaxx/api-ts';
import { createRoute, deleteRoute, fetchRoute, fetchRoutes, updateRoute } from '@entities/routes/api/routes-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageMetadata } from '@shared/api/types';
import { formatDistanceToNowStrict } from 'date-fns';
import { ru } from 'date-fns/locale';
import { RouteForm } from './route-form';
import { parseApiError } from '@shared/api/errors';
import { FilterPopover, type FilterField } from '@shared/ui/filter-popover';

interface EntityChange<T> {
  id: number;
  dto: T | null;
  changeType: 'CREATED' | 'UPDATED' | 'DELETED';
}

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

export function RoutesPage() {
  const queryClient = useQueryClient();
  const { notifyError, notifySuccess } = useToastHelpers();
  const [isCreateOpen, setCreateOpen] = useState(false);
  const [editingRouteId, setEditingRouteId] = useState<number | null>(null);
  const [editingPayload, setEditingPayload] = useState<Route | null>(null);

  const { state, handlers, request } = useServerTable({ size: 20 });

  const routesQuery = useQuery({
    queryKey: queryKeys.routes.list(request),
    queryFn: () => fetchRoutes(request),
    placeholderData: keepPreviousData,
    staleTime: 5_000
  });

  const meta = routesQuery.data?.meta ?? { ...defaultMeta, page: state.page, size: state.size };
  const routes = routesQuery.data?.data ?? [];

  const columns = useMemo<ColumnDef<Route>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        cell: ({ row }) => <span className="font-medium">#{row.original.id}</span>,
        enableSorting: true
      },
      {
        id: 'vehicle',
        header: 'Транспорт',
        cell: ({ row }) => (
          <div className="flex flex-col">
            <span>{row.original.vehicle?.gosNumber ?? '—'}</span>
            <span className="text-xs text-muted-foreground">
              Водитель: {row.original.vehicle?.driver?.firstName ?? '—'} {row.original.vehicle?.driver?.lastName ?? ''}
            </span>
          </div>
        )
      },
      {
        id: 'creationTime',
        header: 'Создан',
        enableSorting: true,
        cell: ({ row }) => {
          const date = formatDateParts(row.original.creationTime);
          return (
            <div className="flex flex-col text-sm">
              <span>{date.absolute}</span>
              {date.relative ? (
                <span className="text-xs text-muted-foreground">{date.relative}</span>
              ) : null}
            </div>
          );
        }
      },
      {
        id: 'plannedStartTime',
        header: 'Окно исполнения',
        cell: ({ row }) => {
          const start = formatDateParts(row.original.plannedStartTime);
          const end = formatDateParts(row.original.plannedEndTime);
          return (
            <div className="flex flex-col text-sm">
              <span>
                Начало: {start.absolute}
                {start.relative ? (
                  <span className="text-xs text-muted-foreground"> ({start.relative})</span>
                ) : null}
              </span>
              <span>
                Конец: {end.absolute}
                {end.relative ? (
                  <span className="text-xs text-muted-foreground"> ({end.relative})</span>
                ) : null}
              </span>
            </div>
          );
        }
      },
      {
        id: 'mileageInKm',
        header: 'Пробег, км',
        enableSorting: true,
        cell: ({ row }) => (row.original.mileageInKm ?? 0).toFixed(3)
      },
      {
        id: 'routePointsSize',
        header: 'Точек',
        cell: ({ row }) => row.original.routePoints?.length ?? 0
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex items-center justify-end gap-2">
            <Button size="sm" variant="secondary" type="button" onClick={() => openEdit(row.original.id)}>
              Редактировать
            </Button>
            <Button
              size="sm"
              variant="ghost"
              type="button"
              onClick={() => handleDelete(row.original)}
            >
              Удалить
            </Button>
          </div>
        )
      }
    ],
    []
  );

  const createMutation = useMutation({
    mutationFn: (payload: SaveRoute) => createRoute(payload),
    onSuccess: () => {
      notifySuccess('Маршрут создан');
      setCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.routes.all });
    },
    onError: (error) => {
      void handleApiError(error, notifyError);
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SaveRoute }) => updateRoute(id, payload),
    onSuccess: () => {
      notifySuccess('Маршрут обновлён');
      setEditingRouteId(null);
      setEditingPayload(null);
      queryClient.invalidateQueries({ queryKey: queryKeys.routes.all });
    },
    onError: (error) => {
      void handleApiError(error, notifyError);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteRoute(id),
    onSuccess: () => {
      notifySuccess('Маршрут удалён');
      queryClient.invalidateQueries({ queryKey: queryKeys.routes.all });
    },
    onError: (error) => {
      void handleApiError(error, notifyError);
    }
  });

  const handleRouteEvent = useCallback(
    (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body) as EntityChange<Route>;
        if (!payload) {
          return;
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.routes.all });
        if (payload.changeType === 'CREATED') {
          notifySuccess('Добавлен новый маршрут');
        }
      } catch (error) {
        console.error('Ошибка разбора события маршрута', error);
      }
    },
    [notifySuccess, queryClient]
  );

  useWebSocketSubscription('/topic/routes', handleRouteEvent);

  const openEdit = async (id: number) => {
    try {
      setEditingRouteId(id);
      const route = await queryClient.fetchQuery({
        queryKey: queryKeys.routes.detail(id),
        queryFn: () => fetchRoute(id)
      });
      setEditingPayload(route);
    } catch (error) {
      notifyError('Не удалось загрузить маршрут', error instanceof Error ? error.message : undefined);
      setEditingRouteId(null);
      setEditingPayload(null);
    }
  };

  const handleDelete = (route: Route) => {
    if (!route.id) return;
    if (window.confirm(`Удалить маршрут #${route.id}?`)) {
      deleteMutation.mutate(route.id);
    }
  };

  const isLoading = routesQuery.isLoading;
  const isFetching = routesQuery.isFetching && !routesQuery.isLoading;

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-semibold">Маршруты</h1>
          <p className="text-muted-foreground">Управление созданием, редактированием и мониторингом маршрутов.</p>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          Создать маршрут
        </Button>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <FilterPopover
          fields={routeFilterFields}
          values={state.filters}
          onFilterChange={(key, value) => handlers.onFilterChange(key, value)}
          onReset={() => handlers.resetFilters()}
        />
        <Button type="button" size="sm" variant="ghost" onClick={() => handlers.onSortingChange([])}>
          Сбросить сортировку
        </Button>
      </div>

      <DataTable
        data={routes}
        columns={columns}
        meta={meta}
        state={state}
        handlers={handlers}
        isLoading={isLoading}
        isFetching={isFetching}
        emptyState={
          state.filters.vehicleId ? 'Нет маршрутов для выбранного транспорта' : 'Маршруты отсутствуют'
        }
      />

      <Dialog open={isCreateOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Создание маршрута</DialogTitle>
          </DialogHeader>
          <RouteForm
            onSubmit={(payload) => createMutation.mutateAsync(payload).then(() => undefined)}
            onCancel={() => setCreateOpen(false)}
            isSubmitting={createMutation.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(editingRouteId)} onOpenChange={(open) => !open && handleCloseEdit()}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Редактирование маршрута</DialogTitle>
          </DialogHeader>
          {editingRouteId && !editingPayload ? (
            <p className="text-sm text-muted-foreground">Загрузка данных маршрута…</p>
          ) : null}
          {editingPayload ? (
            <RouteForm
              initialRoute={editingPayload}
              onSubmit={(payload) =>
                editingPayload?.id
                  ? updateMutation.mutateAsync({ id: editingPayload.id, payload }).then(() => undefined)
                  : Promise.resolve()
              }
              onCancel={handleCloseEdit}
              isSubmitting={updateMutation.isPending}
            />
          ) : null}
        </DialogContent>
      </Dialog>
    </div>
  );

  function handleCloseEdit() {
    setEditingRouteId(null);
    setEditingPayload(null);
  }
}

const routeFilterFields: FilterField[] = [
  { key: 'id', label: 'ID', type: 'number' },
  { key: 'vehicleId', label: 'ID транспорта', type: 'number' },
  { key: 'vehicle.driverId', label: 'ID водителя', type: 'number' },
  { key: 'vehicle.gosNumber', label: 'Гос. номер', type: 'text' },
  { key: 'mileageInKm', label: 'Пробег, км', type: 'number' },
  { key: 'creationTime', label: 'Дата создания (UTC)', type: 'datetime' },
  { key: 'plannedStartTime', label: 'Плановое начало (UTC)', type: 'datetime' },
  { key: 'plannedEndTime', label: 'Плановое окончание (UTC)', type: 'datetime' }
];

function formatDateParts(value?: Date) {
  if (!value) {
    return { absolute: '—', relative: '' };
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return { absolute: '—', relative: '' };
  }
  const formatter = new Intl.DateTimeFormat('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  });
  const relative = formatDistanceToNowStrict(date, { locale: ru, addSuffix: true });
  return { absolute: formatter.format(date), relative };
}

async function handleApiError(
  error: unknown,
  notifyError: (title: string, description?: string) => void
): Promise<void> {
  const parsed = await parseApiError(error);
  notifyError('Ошибка', parsed.message);
}
