import { useCallback, useMemo, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServerTable } from '@shared/lib/server-table';
import { DataTable } from '@shared/ui/data-table';
import { Button } from '@shared/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@shared/ui/dialog';
import { useToastHelpers } from '@app/providers/toast-provider';
import { useWebSocketSubscription } from '@app/providers/websocket-provider';
import type { SaveVehicle, Vehicle } from '@rendaxx/api-ts';
import type { IMessage } from '@stomp/stompjs';
import { createVehicle, deleteVehicle, fetchVehicle, fetchVehicles, updateVehicle } from '@entities/vehicles/api/vehicles-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageMetadata } from '@shared/api/types';
import { parseApiError } from '@shared/api/errors';
import { VehicleForm } from './vehicle-form';
import { FilterPopover, type FilterField } from '@shared/ui/filter-popover';

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

export function VehiclesPage() {
  const queryClient = useQueryClient();
  const { notifyError, notifySuccess } = useToastHelpers();
  const { state, handlers, request } = useServerTable({ size: 20 });

  const [isCreateOpen, setCreateOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);

  const vehiclesQuery = useQuery({
    queryKey: queryKeys.vehicles.list(request),
    queryFn: () => fetchVehicles(request),
    placeholderData: keepPreviousData,
    staleTime: 5_000
  });

  const meta = vehiclesQuery.data?.meta ?? { ...defaultMeta, page: state.page, size: state.size };
  const vehicles = vehiclesQuery.data?.data ?? [];

  const columns = useMemo<ColumnDef<Vehicle>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        enableSorting: true,
        cell: ({ row }) => <span className="font-medium">#{row.original.id}</span>
      },
      {
        accessorKey: 'gosNumber',
        header: 'Гос. номер'
      },
      {
        id: 'driver',
        header: 'Водитель',
        cell: ({ row }) => {
          const driver = row.original.driver;
          if (!driver) {
            return '—';
          }
          return `${driver.lastName ?? ''} ${driver.firstName ?? ''}`.trim() || `ID ${driver.id}`;
        }
      },
      {
        accessorKey: 'tonnageInTons',
        header: 'Грузопод., т',
        enableSorting: true,
        cell: ({ row }) => formatNumber(row.original.tonnageInTons)
      },
      {
        id: 'dimensions',
        header: 'Габариты кузова, м',
        cell: ({ row }) => {
          const v = row.original;
          return `${formatNumber(v.bodyHeightInMeters)} × ${formatNumber(v.bodyWidthInMeters)} × ${formatNumber(v.bodyLengthInCubicMeters)}`;
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
    mutationFn: (payload: SaveVehicle) => createVehicle(payload),
    onSuccess: () => {
      notifySuccess('Транспорт добавлен');
      setCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.vehicles.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SaveVehicle }) => updateVehicle(id, payload),
    onSuccess: () => {
      notifySuccess('Транспорт обновлён');
      closeEdit();
      queryClient.invalidateQueries({ queryKey: queryKeys.vehicles.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteVehicle(id),
    onSuccess: () => {
      notifySuccess('Транспорт удалён');
      queryClient.invalidateQueries({ queryKey: queryKeys.vehicles.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const handleVehicleEvent = useCallback(
    (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body) as EntityChange<Vehicle>;
        if (!payload) {
          return;
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.vehicles.all });
        if (payload.changeType === 'CREATED') {
          notifySuccess('Добавлено новое транспортное средство');
        }
      } catch (error) {
        console.error('Ошибка разбора события транспорта', error);
      }
    },
    [notifySuccess, queryClient]
  );

  useWebSocketSubscription('/topic/vehicles', handleVehicleEvent);

  const openEdit = async (id: number) => {
    try {
      setEditingId(id);
      const vehicle = await queryClient.fetchQuery({
        queryKey: queryKeys.vehicles.detail(id),
        queryFn: () => fetchVehicle(id)
      });
      setEditingVehicle(vehicle);
    } catch (error) {
      const parsed = await parseApiError(error);
      notifyError('Не удалось загрузить транспорт', parsed.message);
      closeEdit();
    }
  };

  const handleDelete = (vehicle: Vehicle) => {
    if (!vehicle.id) return;
    if (window.confirm(`Удалить транспорт #${vehicle.id}?`)) {
      deleteMutation.mutate(vehicle.id);
    }
  };

  const closeEdit = () => {
    setEditingId(null);
    setEditingVehicle(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-semibold">Транспорт</h1>
          <p className="text-muted-foreground">Управляйте транспортными средствами, их параметрами и привязкой к водителям.</p>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          Добавить транспорт
        </Button>
      </div>

      <FilterPopover
        fields={vehicleFilterFields}
        values={state.filters}
        onFilterChange={(key, value) => handlers.onFilterChange(key, value)}
        onReset={() => handlers.resetFilters()}
      />

      <DataTable
        data={vehicles}
        columns={columns}
        meta={meta}
        state={state}
        handlers={handlers}
        isLoading={vehiclesQuery.isLoading}
        isFetching={vehiclesQuery.isFetching && !vehiclesQuery.isLoading}
        emptyState="Транспортные средства отсутствуют"
      />

      <Dialog open={isCreateOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Новое транспортное средство</DialogTitle>
          </DialogHeader>
          <VehicleForm
            onSubmit={(payload) => createMutation.mutateAsync(payload).then(() => undefined)}
            onCancel={() => setCreateOpen(false)}
            isSubmitting={createMutation.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(editingId)} onOpenChange={(open) => !open && closeEdit()}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Редактирование транспорта</DialogTitle>
          </DialogHeader>
          {editingId && !editingVehicle ? (
            <p className="text-sm text-muted-foreground">Загрузка данных транспорта…</p>
          ) : null}
          {editingVehicle ? (
            <VehicleForm
              initialVehicle={editingVehicle}
              onSubmit={(payload) =>
                editingVehicle?.id
                  ? updateMutation.mutateAsync({ id: editingVehicle.id, payload }).then(() => undefined)
                  : Promise.resolve()
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

const vehicleFilterFields: FilterField[] = [
  { key: 'id', label: 'ID', type: 'number' },
  { key: 'gosNumber', label: 'Гос. номер' },
  { key: 'driverId', label: 'ID водителя', type: 'number' },
  { key: 'tonnageInTons', label: 'Грузоподъёмность, т', type: 'number' },
  { key: 'bodyHeightInMeters', label: 'Высота, м', type: 'number' },
  { key: 'bodyWidthInMeters', label: 'Ширина, м', type: 'number' },
  { key: 'bodyLengthInCubicMeters', label: 'Длина кузова, м³', type: 'number' }
];

function formatNumber(value?: number | null) {
  if (value == null) {
    return '—';
  }
  return Number(value).toFixed(2);
}
