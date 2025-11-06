import { FormProvider, useForm } from 'react-hook-form';
import { z } from 'zod';
import type { RoutePoint, SaveRoutePoint } from '@rendaxx/api-ts';
import { Button } from '@shared/ui/button';
import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';
import { Input } from '@shared/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchRetailPoints } from '@entities/retail-points/api/retail-points-api';
import { fetchRoutes } from '@entities/routes/api/routes-api';
import { fetchOrders } from '@entities/orders/api/orders-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageResult } from '@shared/api/types';
import { OperationType } from '@rendaxx/api-ts';
import { Plus, Trash2 } from 'lucide-react';

const schema = z.object({
  routeId: z.string().min(1, 'Выберите маршрут'),
  retailPointId: z.string().min(1, 'Выберите торговую точку'),
  operationType: z.nativeEnum(OperationType, { errorMap: () => ({ message: 'Выберите тип операции' }) }),
  orderIds: z.array(z.string()).default([]),
  plannedStartTime: z.string().min(1, 'Укажите время начала'),
  plannedEndTime: z.string().min(1, 'Укажите время окончания'),
  orderNumber: z.string().min(1, 'Укажите порядковый номер')
});

type RoutePointFormValues = z.input<typeof schema>;

interface RoutePointFormProps {
  initialRoutePoint?: RoutePoint;
  onSubmit: (payload: SaveRoutePoint) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

interface Option {
  value: string;
  label: string;
}

export function RoutePointForm({ initialRoutePoint, onSubmit, onCancel, isSubmitting }: RoutePointFormProps) {
  const defaultValues = useMemo<RoutePointFormValues>(() => {
    if (!initialRoutePoint) {
      return {
        routeId: '',
        retailPointId: '',
        operationType: OperationType.Visit,
        orderIds: [],
        plannedStartTime: formatDateTimeInput(),
        plannedEndTime: formatDateTimeInput(),
        orderNumber: '0'
      };
    }
    return {
      routeId: initialRoutePoint.routeId?.toString() ?? '',
      retailPointId: initialRoutePoint.retailPoint?.id?.toString() ?? '',
      operationType: initialRoutePoint.operationType ?? OperationType.Visit,
      orderIds: (initialRoutePoint.orders ?? []).map((order) => order.id?.toString() ?? '').filter(Boolean),
      plannedStartTime: formatDateTimeInput(initialRoutePoint.plannedStartTime),
      plannedEndTime: formatDateTimeInput(initialRoutePoint.plannedEndTime),
      orderNumber: initialRoutePoint.orderNumber?.toString() ?? '0'
    };
  }, [initialRoutePoint]);

  const form = useForm<RoutePointFormValues>({
    defaultValues,
    mode: 'onBlur'
  });

  const routesQuery = useQuery({
    queryKey: queryKeys.routes.list({ page: 0, size: 100 }),
    queryFn: () => fetchRoutes({ page: 0, size: 100 }),
    staleTime: 60_000
  });

  const retailPointsQuery = useQuery({
    queryKey: queryKeys.retailPoints.list({ page: 0, size: 100 }),
    queryFn: () => fetchRetailPoints({ page: 0, size: 100 }),
    staleTime: 60_000
  });

  const ordersQuery = useQuery({
    queryKey: queryKeys.orders.list({ page: 0, size: 100 }),
    queryFn: () => fetchOrders({ page: 0, size: 100 }),
    staleTime: 60_000
  });

  const routeOptions = toRouteOptions(routesQuery.data);
  const retailPointOptions = toPointOptions(retailPointsQuery.data);
  const orderOptions = toOrderOptions(ordersQuery.data);

  const handleSubmit = async (values: RoutePointFormValues) => {
    const parsed = schema.safeParse(values);
    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        form.setError(issue.path.join('.') as any, { message: issue.message });
      });
      return;
    }

    const payload: SaveRoutePoint = {
      id: initialRoutePoint?.id,
      routeId: Number(parsed.data.routeId),
      retailPointId: Number(parsed.data.retailPointId),
      operationType: parsed.data.operationType,
      orderIds: parseOrderIds(parsed.data.orderIds),
      plannedStartTime: toUtcDate(parsed.data.plannedStartTime),
      plannedEndTime: toUtcDate(parsed.data.plannedEndTime),
      orderNumber: Number(parsed.data.orderNumber)
    };

    await onSubmit(payload);
  };

  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>{initialRoutePoint ? 'Редактирование точки маршрута' : 'Новая точка маршрута'}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <FormField
              control={form.control}
              name="routeId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Маршрут</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value} disabled={routeOptions.length === 0}>
                    <SelectTrigger>
                      <SelectValue placeholder="Выберите маршрут" />
                    </SelectTrigger>
                    <SelectContent>
                      {routeOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="retailPointId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Торговая точка</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value} disabled={retailPointOptions.length === 0}>
                    <SelectTrigger>
                      <SelectValue placeholder="Выберите точку" />
                    </SelectTrigger>
                    <SelectContent>
                      {retailPointOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="operationType"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Тип операции</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {Object.entries(operationLabels).map(([value, label]) => (
                        <SelectItem key={value} value={value}>
                          {label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="plannedStartTime"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Время начала (UTC)</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="plannedEndTime"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Время окончания (UTC)</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="orderNumber"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Порядковый номер</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="orderIds"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Связанные заказы</FormLabel>
                  <OrdersInlinePicker value={field.value ?? []} onChange={field.onChange} options={orderOptions} />
                  <FormDescription>Выберите заказы, относящиеся к точке маршрута.</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
          </CardContent>
        </Card>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Сохранение…' : initialRoutePoint ? 'Сохранить изменения' : 'Добавить точку'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}

function OrdersInlinePicker({
  value,
  onChange,
  options
}: {
  value: string[];
  onChange: (next: string[]) => void;
  options: Option[];
}) {
  const [pending, setPending] = useState<string>('');
  const selectedSet = new Set(value ?? []);
  const available = options.filter((o) => !selectedSet.has(o.value));
  const lookup = useMemo(() => new Map(options.map((o) => [o.value, o.label])), [options]);

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <Select value={pending} onValueChange={setPending}>
          <SelectTrigger className="w-64">
            <SelectValue placeholder={available.length ? 'Выберите заказ' : 'Нет доступных заказов'} />
          </SelectTrigger>
          <SelectContent>
            {available.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Button
          type="button"
          size="sm"
          variant="secondary"
          disabled={!pending}
          onClick={() => {
            if (pending && !selectedSet.has(pending)) {
              onChange([...(value ?? []), pending]);
              setPending('');
            }
          }}
        >
          <Plus className="h-4 w-4" />
        </Button>
      </div>

      <div className="overflow-hidden rounded-md border border-border">
        <table className="w-full border-collapse text-sm">
          <thead className="bg-muted/60 text-left text-xs uppercase tracking-wide text-muted-foreground">
            <tr>
              <th className="px-3 py-2 font-semibold">ID</th>
              <th className="px-3 py-2 font-semibold">Заказ</th>
              <th className="px-3 py-2 font-semibold text-right">Действия</th>
            </tr>
          </thead>
          <tbody>
            {(value ?? []).length === 0 ? (
              <tr>
                <td colSpan={3} className="px-3 py-4 text-center text-muted-foreground">
                  Заказы не выбраны
                </td>
              </tr>
            ) : (
              (value ?? []).map((id) => (
                <tr key={id} className="border-t border-border">
                  <td className="px-3 py-2">#{id}</td>
                  <td className="px-3 py-2">{lookup.get(id) ?? id}</td>
                  <td className="px-3 py-2 text-right">
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      onClick={() => onChange((value ?? []).filter((x) => x !== id))}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function toRouteOptions(result?: PageResult<any>): Option[] {
  if (!result) {
    return [];
  }
  return result.data
    .filter((route) => route.id != null)
    .map((route) => ({
      value: String(route.id),
      label: `Маршрут #${route.id} (${route.vehicle?.gosNumber ?? 'без ТС'})`
    }));
}

function toPointOptions(result?: PageResult<any>): Option[] {
  if (!result) {
    return [];
  }
  return result.data
    .filter((point) => point.id != null)
    .map((point) => ({
      value: String(point.id),
      label: point.name ?? `Точка #${point.id}`
    }));
}

function toOrderOptions(result?: PageResult<any>): Option[] {
  if (!result) {
    return [];
  }
  return result.data
    .filter((order) => order.id != null)
    .map((order) => ({ value: order.id!.toString(), label: `#${order.id} (${order.goodsType ?? '—'})` }));
}

function parseOrderIds(values?: string[]): number[] {
  if (!values || values.length === 0) {
    return [];
  }
  return values.map((value) => Number(value)).filter((num) => !Number.isNaN(num));
}

function formatDateTimeInput(value?: Date | string | null): string {
  const date = value ? new Date(value) : new Date();
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return date.toISOString().slice(0, 16);
}

function toUtcDate(value: string): Date {
  if (!value) {
    throw new Error('Дата не указана');
  }
  const normalized = value.endsWith('Z') ? value : `${value}${value.length === 16 ? ':00' : ''}Z`;
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) {
    throw new Error(`Некорректная дата: ${value}`);
  }
  return date;
}

function formatDateTimeLocal(value?: Date | string | null): string {
  const date = value ? new Date(value) : new Date();
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const pad = (n: number) => n.toString().padStart(2, '0');
  const yyyy = date.getUTCFullYear();
  const MM = pad(date.getUTCMonth() + 1);
  const dd = pad(date.getUTCDate());
  const hh = pad(date.getUTCHours());
  const mm = pad(date.getUTCMinutes());
  return `${yyyy}-${MM}-${dd}T${hh}:${mm}`;
}

const operationLabels: Record<OperationType, string> = {
  [OperationType.Load]: 'Погрузка',
  [OperationType.Unload]: 'Выгрузка',
  [OperationType.Visit]: 'Посещение'
};
