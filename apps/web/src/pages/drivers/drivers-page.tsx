import { useCallback, useMemo, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServerTable } from '@shared/lib/server-table';
import { DataTable } from '@shared/ui/data-table';
import { Button } from '@shared/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@shared/ui/dialog';
import { useToastHelpers } from '@app/providers/toast-provider';
import { useWebSocketSubscription } from '@app/providers/websocket-provider';
import type { Driver, SaveDriver } from '@rendaxx/api-ts';
import type { IMessage } from '@stomp/stompjs';
import { createDriver, deleteDriver, fetchDriver, fetchDrivers, updateDriver } from '@entities/drivers/api/drivers-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageMetadata } from '@shared/api/types';
import { parseApiError } from '@shared/api/errors';
import { DriverForm } from './driver-form';
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

export function DriversPage() {
  const queryClient = useQueryClient();
  const { notifyError, notifySuccess } = useToastHelpers();
  const { state, handlers, request } = useServerTable({ size: 20 });

  const [isCreateOpen, setCreateOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingDriver, setEditingDriver] = useState<Driver | null>(null);

  const driversQuery = useQuery({
    queryKey: queryKeys.drivers.list(request),
    queryFn: () => fetchDrivers(request),
    placeholderData: keepPreviousData,
    staleTime: 5_000
  });

  const meta = driversQuery.data?.meta ?? { ...defaultMeta, page: state.page, size: state.size };
  const drivers = driversQuery.data?.data ?? [];

  const columns = useMemo<ColumnDef<Driver>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        enableSorting: true,
        cell: ({ row }) => <span className="font-medium">#{row.original.id}</span>
      },
      {
        id: 'name',
        header: 'ФИО',
        cell: ({ row }) => (
          <span>
            {row.original.lastName ?? ''} {row.original.firstName ?? ''} {row.original.middleName ?? ''}
          </span>
        )
      },
      {
        accessorKey: 'passport',
        header: 'Паспорт'
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
    mutationFn: (payload: SaveDriver) => createDriver(payload),
    onSuccess: () => {
      notifySuccess('Водитель создан');
      setCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.drivers.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SaveDriver }) => updateDriver(id, payload),
    onSuccess: () => {
      notifySuccess('Водитель обновлён');
      closeEdit();
      queryClient.invalidateQueries({ queryKey: queryKeys.drivers.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteDriver(id),
    onSuccess: () => {
      notifySuccess('Водитель удалён');
      queryClient.invalidateQueries({ queryKey: queryKeys.drivers.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const handleDriverEvent = useCallback(
    (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body) as EntityChange<Driver>;
        if (!payload) {
          return;
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.drivers.all });
        if (payload.changeType === 'CREATED') {
          notifySuccess('Добавлен новый водитель');
        }
      } catch (error) {
        console.error('Ошибка разбора события водителя', error);
      }
    },
    [notifySuccess, queryClient]
  );

  useWebSocketSubscription('/topic/drivers', handleDriverEvent);

  const openEdit = async (id: number) => {
    try {
      setEditingId(id);
      const driver = await queryClient.fetchQuery({
        queryKey: queryKeys.drivers.detail(id),
        queryFn: () => fetchDriver(id)
      });
      setEditingDriver(driver);
    } catch (error) {
      const parsed = await parseApiError(error);
      notifyError('Не удалось загрузить водителя', parsed.message);
      closeEdit();
    }
  };

  const handleDelete = (driver: Driver) => {
    if (!driver.id) return;
    if (window.confirm(`Удалить водителя #${driver.id}?`)) {
      deleteMutation.mutate(driver.id);
    }
  };

  const closeEdit = () => {
    setEditingId(null);
    setEditingDriver(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-semibold">Водители</h1>
          <p className="text-muted-foreground">Создавайте, обновляйте и отслеживайте изменения данных водителей.</p>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          Добавить водителя
        </Button>
      </div>

      <FilterPopover
        fields={driverFilterFields}
        values={state.filters}
        onFilterChange={(key, value) => handlers.onFilterChange(key, value)}
        onReset={() => handlers.resetFilters()}
      />

      <DataTable
        data={drivers}
        columns={columns}
        meta={meta}
        state={state}
        handlers={handlers}
        isLoading={driversQuery.isLoading}
        isFetching={driversQuery.isFetching && !driversQuery.isLoading}
        emptyState="Водители не найдены"
      />

      <Dialog open={isCreateOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Новый водитель</DialogTitle>
          </DialogHeader>
          <DriverForm
            onSubmit={(payload) => createMutation.mutateAsync(payload).then(() => undefined)}
            onCancel={() => setCreateOpen(false)}
            isSubmitting={createMutation.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(editingId)} onOpenChange={(open) => !open && closeEdit()}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Редактирование водителя</DialogTitle>
          </DialogHeader>
          {editingId && !editingDriver ? (
            <p className="text-sm text-muted-foreground">Загрузка данных водителя…</p>
          ) : null}
          {editingDriver ? (
            <DriverForm
              initialDriver={editingDriver}
              onSubmit={(payload) =>
                editingDriver?.id
                  ? updateMutation.mutateAsync({ id: editingDriver.id, payload }).then(() => undefined)
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

const driverFilterFields: FilterField[] = [
  { key: 'id', label: 'ID', type: 'number' },
  { key: 'firstName', label: 'Имя' },
  { key: 'lastName', label: 'Фамилия' },
  { key: 'middleName', label: 'Отчество' },
  { key: 'passport', label: 'Паспорт' }
];
