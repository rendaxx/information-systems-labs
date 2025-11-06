import { useCallback, useMemo, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServerTable } from '@shared/lib/server-table';
import { DataTable } from '@shared/ui/data-table';
import { Button } from '@shared/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@shared/ui/dialog';
import { useToastHelpers } from '@app/providers/toast-provider';
import { useWebSocketSubscription } from '@app/providers/websocket-provider';
import type { Order, SaveOrder } from '@rendaxx/api-ts';
import type { IMessage } from '@stomp/stompjs';
import { createOrder, deleteOrder, fetchOrder, fetchOrders, updateOrder } from '@entities/orders/api/orders-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageMetadata } from '@shared/api/types';
import { OrderForm } from './order-form';
import { parseApiError } from '@shared/api/errors';

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

export function OrdersPage() {
  const queryClient = useQueryClient();
  const { notifyError, notifySuccess } = useToastHelpers();
  const { state, handlers, request } = useServerTable({ size: 20 });

  const [isCreateOpen, setCreateOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingOrder, setEditingOrder] = useState<Order | null>(null);

  const ordersQuery = useQuery({
    queryKey: queryKeys.orders.list(request),
    queryFn: () => fetchOrders(request),
    keepPreviousData: true,
    staleTime: 5_000
  });

  const meta = ordersQuery.data?.meta ?? { ...defaultMeta, page: state.page, size: state.size };
  const orders = ordersQuery.data?.data ?? [];

  const columns = useMemo<ColumnDef<Order>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        enableSorting: true,
        cell: ({ row }) => <span className="font-medium">#{row.original.id}</span>
      },
      {
        accessorKey: 'goodsType',
        header: 'Тип товара'
      },
      {
        accessorKey: 'weightInKg',
        header: 'Вес, кг',
        enableSorting: true,
        cell: ({ row }) => (row.original.weightInKg ?? 0).toFixed(2)
      },
      {
        accessorKey: 'volumeInCubicMeters',
        header: 'Объём, м³',
        enableSorting: true,
        cell: ({ row }) => (row.original.volumeInCubicMeters ?? 0).toFixed(2)
      },
      {
        id: 'temperature',
        header: 'Температура',
        cell: ({ row }) => {
          const min = row.original.minTemperature;
          const max = row.original.maxTemperature;
          if (min == null && max == null) {
            return '—';
          }
          if (min != null && max != null) {
            return `${min}…${max} °C`;
          }
          return `${min ?? max} °C`;
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
    mutationFn: (payload: SaveOrder) => createOrder(payload),
    onSuccess: () => {
      notifySuccess('Заказ создан');
      setCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SaveOrder }) => updateOrder(id, payload),
    onSuccess: () => {
      notifySuccess('Заказ обновлён');
      closeEdit();
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteOrder(id),
    onSuccess: () => {
      notifySuccess('Заказ удалён');
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
    },
    onError: (error) => void handleApiError(error)
  });

  const handleOrderEvent = useCallback(
    (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body) as EntityChange<Order>;
        if (!payload) {
          return;
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
        if (payload.changeType === 'CREATED') {
          notifySuccess('Добавлен новый заказ');
        }
      } catch (error) {
        console.error('Ошибка разбора события заказа', error);
      }
    },
    [notifySuccess, queryClient]
  );

  useWebSocketSubscription('/topic/orders', handleOrderEvent);

  const openEdit = async (id: number) => {
    try {
      setEditingId(id);
      const order = await queryClient.fetchQuery({
        queryKey: queryKeys.orders.detail(id),
        queryFn: () => fetchOrder(id)
      });
      setEditingOrder(order);
    } catch (error) {
      const parsed = await parseApiError(error);
      notifyError('Не удалось загрузить заказ', parsed.message);
      closeEdit();
    }
  };

  const handleDelete = (order: Order) => {
    if (!order.id) return;
    if (window.confirm(`Удалить заказ #${order.id}?`)) {
      deleteMutation.mutate(order.id);
    }
  };

  const closeEdit = () => {
    setEditingId(null);
    setEditingOrder(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-semibold">Заказы</h1>
          <p className="text-muted-foreground">Управляйте заказами — создавайте, обновляйте и отслеживайте обновления в реальном времени.</p>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          Создать заказ
        </Button>
      </div>

      <DataTable
        data={orders}
        columns={columns}
        meta={meta}
        state={state}
        handlers={handlers}
        isLoading={ordersQuery.isLoading}
        isFetching={ordersQuery.isFetching && !ordersQuery.isLoading}
        emptyState={orders.length === 0 ? 'Заказы отсутствуют' : undefined}
      />

      <Dialog open={isCreateOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Создание заказа</DialogTitle>
          </DialogHeader>
          <OrderForm
            onSubmit={(payload) => createMutation.mutateAsync(payload)}
            onCancel={() => setCreateOpen(false)}
            isSubmitting={createMutation.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(editingId)} onOpenChange={(open) => !open && closeEdit()}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Редактирование заказа</DialogTitle>
          </DialogHeader>
          {editingId && !editingOrder ? (
            <p className="text-sm text-muted-foreground">Загрузка данных заказа…</p>
          ) : null}
          {editingOrder ? (
            <OrderForm
              initialOrder={editingOrder}
              onSubmit={(payload) =>
                editingOrder?.id ? updateMutation.mutateAsync({ id: editingOrder.id, payload }) : Promise.resolve()
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
