import { useCallback, useMemo, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServerTable } from '@shared/lib/server-table';
import { DataTable } from '@shared/ui/data-table';
import { Button } from '@shared/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@shared/ui/dialog';
import { useToastHelpers } from '@app/providers/toast-provider';
import { useWebSocketSubscription } from '@app/providers/websocket-provider';
import { PointType, type RetailPoint, type SaveRetailPoint } from '@rendaxx/api-ts';
import type { IMessage } from '@stomp/stompjs';
import {
  createRetailPoint,
  deleteRetailPoint,
  fetchNearestRetailPoints,
  fetchRetailPoint,
  fetchRetailPoints,
  updateRetailPoint
} from '@entities/retail-points/api/retail-points-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageMetadata } from '@shared/api/types';
import { parseApiError } from '@shared/api/errors';
import { RetailPointForm } from './retail-point-form';
import { Input } from '@shared/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { fetchTopRetailPoints } from '@entities/route-points/api/route-points-api';

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

export function RetailPointsPage() {
  const queryClient = useQueryClient();
  const { notifyError, notifySuccess } = useToastHelpers();
  const { state, handlers, request } = useServerTable({ size: 20 });

  const [isCreateOpen, setCreateOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingPoint, setEditingPoint] = useState<RetailPoint | null>(null);

  const [nearestId, setNearestId] = useState<string>('');
  const [nearestLimit, setNearestLimit] = useState<number>(5);
  const [topLimit, setTopLimit] = useState<number>(5);

  const pointsQuery = useQuery({
    queryKey: queryKeys.retailPoints.list(request),
    queryFn: () => fetchRetailPoints(request),
    keepPreviousData: true,
    staleTime: 5_000
  });

  const meta = pointsQuery.data?.meta ?? { ...defaultMeta, page: state.page, size: state.size };
  const retailPoints = pointsQuery.data?.data ?? [];

  const columns = useMemo<ColumnDef<RetailPoint>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        enableSorting: true,
        cell: ({ row }) => <span className="font-medium">#{row.original.id}</span>
      },
      {
        accessorKey: 'name',
        header: 'Название'
      },
      {
        accessorKey: 'type',
        header: 'Тип',
        cell: ({ row }) => pointTypeLabels[row.original.type as PointType] ?? row.original.type
      },
      {
        accessorKey: 'timezone',
        header: 'Часовой пояс'
      },
      {
        accessorKey: 'address',
        header: 'Адрес'
      },
      {
        id: 'coordinates',
        header: 'Координаты',
        cell: ({ row }) => {
          const coords = row.original.location;
          if (!coords) {
            return '—';
          }
          return `${coords.latitude?.toFixed(6) ?? '—'}, ${coords.longitude?.toFixed(6) ?? '—'}`;
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
    mutationFn: (payload: SaveRetailPoint) => createRetailPoint({ ...payload }),
    onSuccess: () => {
      notifySuccess('Точка создана');
      setCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.retailPoints.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SaveRetailPoint }) => updateRetailPoint(id, { ...payload }),
    onSuccess: () => {
      notifySuccess('Точка обновлена');
      closeEdit();
      queryClient.invalidateQueries({ queryKey: queryKeys.retailPoints.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteRetailPoint(id),
    onSuccess: () => {
      notifySuccess('Точка удалена');
      queryClient.invalidateQueries({ queryKey: queryKeys.retailPoints.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const handleRetailPointEvent = useCallback(
    (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body) as EntityChange<RetailPoint>;
        if (!payload) {
          return;
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.retailPoints.all });
        if (payload.changeType === 'CREATED') {
          notifySuccess('Добавлена новая торговая точка');
        }
      } catch (error) {
        console.error('Ошибка разбора события торговой точки', error);
      }
    },
    [notifySuccess, queryClient]
  );

  useWebSocketSubscription('/topic/retail-points', handleRetailPointEvent);

  const nearestQuery = useQuery({
    queryKey: ['retailPoints', 'nearest', nearestId, nearestLimit],
    queryFn: () => fetchNearestRetailPoints(Number(nearestId), nearestLimit),
    enabled: Boolean(nearestId),
    staleTime: 5_000
  });

  const topQuery = useQuery({
    queryKey: ['routePoints', 'topRetailPoints', topLimit],
    queryFn: () => fetchTopRetailPoints(topLimit),
    staleTime: 5_000
  });

  const openEdit = async (id: number) => {
    try {
      setEditingId(id);
      const point = await queryClient.fetchQuery({
        queryKey: queryKeys.retailPoints.detail(id),
        queryFn: () => fetchRetailPoint(id)
      });
      setEditingPoint(point);
    } catch (error) {
      const parsed = await parseApiError(error);
      notifyError('Не удалось загрузить торговую точку', parsed.message);
      closeEdit();
    }
  };

  const handleDelete = (point: RetailPoint) => {
    if (!point.id) return;
    if (window.confirm(`Удалить торговую точку #${point.id}?`)) {
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
          <h1 className="text-2xl font-semibold">Торговые точки</h1>
          <p className="text-muted-foreground">Управляйте точками, их координатами и временными зонами.</p>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          Добавить точку
        </Button>
      </div>

      <DataTable
        data={retailPoints}
        columns={columns}
        meta={meta}
        state={state}
        handlers={handlers}
        isLoading={pointsQuery.isLoading}
        isFetching={pointsQuery.isFetching && !pointsQuery.isLoading}
        emptyState="Торговые точки не найдены"
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Ближайшие точки</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-wrap gap-3">
              <div className="flex flex-col">
                <label className="text-xs font-medium text-muted-foreground" htmlFor="nearest-point">
                  Базовая точка
                </label>
                <Select value={nearestId} onValueChange={setNearestId}>
                  <SelectTrigger className="w-56" id="nearest-point">
                    <SelectValue placeholder="Выберите точку" />
                  </SelectTrigger>
                  <SelectContent>
                    {retailPoints.map((point) => (
                      <SelectItem key={point.id} value={String(point.id)}>
                        {point.name ?? `Точка #${point.id}`}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex flex-col">
                <label className="text-xs font-medium text-muted-foreground" htmlFor="nearest-limit">
                  Количество
                </label>
                <Input
                  id="nearest-limit"
                  type="number"
                  min={1}
                  value={nearestLimit}
                  onChange={(event) => setNearestLimit(Math.max(1, Number(event.target.value)))}
                  className="w-24"
                />
              </div>
              <Button type="button" disabled={!nearestId} onClick={() => nearestQuery.refetch()}>
                Обновить
              </Button>
            </div>

            <ul className="space-y-2 text-sm">
              {nearestQuery.isLoading ? (
                <li className="text-muted-foreground">Загрузка…</li>
              ) : nearestQuery.data && nearestQuery.data.length > 0 ? (
                nearestQuery.data.map((point) => (
                  <li key={point.id} className="rounded-md border border-border px-3 py-2">
                    <div className="font-medium">{point.name ?? `Точка #${point.id}`}</div>
                    <div className="text-xs text-muted-foreground">{point.address}</div>
                  </li>
                ))
              ) : (
                <li className="text-muted-foreground">Нет данных для отображения</li>
              )}
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Топ посещаемых точек</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Input
                type="number"
                min={1}
                value={topLimit}
                onChange={(event) => setTopLimit(Math.max(1, Number(event.target.value)))}
                className="w-24"
              />
              <Button type="button" onClick={() => topQuery.refetch()}>
                Обновить
              </Button>
            </div>
            <ul className="space-y-2 text-sm">
              {topQuery.isLoading ? (
                <li className="text-muted-foreground">Загрузка…</li>
              ) : topQuery.data && topQuery.data.length > 0 ? (
                topQuery.data.map((point) => (
                  <li key={point.id} className="rounded-md border border-border px-3 py-2">
                    <div className="font-medium">{point.name ?? `Точка #${point.id}`}</div>
                    <div className="text-xs text-muted-foreground">{point.address}</div>
                  </li>
                ))
              ) : (
                <li className="text-muted-foreground">Нет данных для отображения</li>
              )}
            </ul>
          </CardContent>
        </Card>
      </div>

      <Dialog open={isCreateOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Новая торговая точка</DialogTitle>
          </DialogHeader>
          <RetailPointForm
            onSubmit={(payload) => createMutation.mutateAsync(payload)}
            onCancel={() => setCreateOpen(false)}
            isSubmitting={createMutation.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(editingId)} onOpenChange={(open) => !open && closeEdit()}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Редактирование торговой точки</DialogTitle>
          </DialogHeader>
          {editingId && !editingPoint ? (
            <p className="text-sm text-muted-foreground">Загрузка данных точки…</p>
          ) : null}
          {editingPoint ? (
            <RetailPointForm
              initialRetailPoint={editingPoint}
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

const pointTypeLabels: Record<PointType, string> = {
  [PointType.SHOP]: 'Магазин',
  [PointType.WAREHOUSE]: 'Склад',
  [PointType.GARAGE]: 'Гараж'
};
