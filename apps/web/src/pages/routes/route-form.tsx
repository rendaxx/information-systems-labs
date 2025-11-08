import { FormProvider, useFieldArray, useForm } from 'react-hook-form';
import { useMemo, useState } from 'react';
import { z } from 'zod';
import type { Route, RoutePoint, SaveRoute } from '@rendaxx/api-ts';
import { OperationType } from '@rendaxx/api-ts';
import { Button } from '@shared/ui/button';
import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/ui/form';
import { Input } from '@shared/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/ui/card';
import { useQuery } from '@tanstack/react-query';
import { fetchVehicles } from '@entities/vehicles/api/vehicles-api';
import { fetchRetailPoints } from '@entities/retail-points/api/retail-points-api';
import { fetchOrders } from '@entities/orders/api/orders-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageResult } from '@shared/api/types';
import { Plus, Trash2 } from 'lucide-react';

const routeSchema = z.object({
  vehicleId: z.string().min(1, 'Выберите транспортное средство'),
  creationTime: z.string().min(1, 'Введите дату создания'),
  plannedStartTime: z.string().min(1, 'Введите плановое начало'),
  plannedEndTime: z.string().min(1, 'Введите плановое окончание'),
  mileageInKm: z.string().min(1, 'Укажите пробег'),
  routePoints: z
    .array(
      z.object({
        id: z.number().optional(),
        retailPointId: z.string().min(1, 'Выберите торговую точку'),
        operationType: z.nativeEnum(OperationType, {
          errorMap: () => ({ message: 'Выберите тип операции' })
        }),
        orderIds: z.array(z.string()).default([]),
        plannedStartTime: z.string().min(1, 'Введите время прибытия'),
        plannedEndTime: z.string().min(1, 'Введите время выбытия'),
        orderNumber: z.coerce.number().min(0, 'Номер точки не может быть отрицательным'),
        routeId: z.number().nullable().optional()
      })
    )
    .min(1, 'Добавьте минимум одну точку маршрута')
});

type RouteFormValues = z.infer<typeof routeSchema>;

const operationTypeLabels: Record<OperationType, string> = {
  [OperationType.LOAD]: 'Погрузка',
  [OperationType.UNLOAD]: 'Выгрузка',
  [OperationType.VISIT]: 'Посещение'
};

interface Option<T extends string | number> {
  value: T;
  label: string;
}

interface RouteFormProps {
  initialRoute?: Route;
  onSubmit: (payload: SaveRoute) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function RouteForm({ initialRoute, onSubmit, onCancel, isSubmitting }: RouteFormProps) {
  const defaultValues = useMemo<RouteFormValues>(() => mapRouteToForm(initialRoute), [initialRoute]);

  const form = useForm<RouteFormValues>({
    defaultValues,
    mode: 'onBlur'
  });

  const {
    fields: pointFields,
    append,
    remove
  } = useFieldArray({
    control: form.control,
    name: 'routePoints'
  });

  const vehiclesQuery = useQuery({
    queryKey: queryKeys.vehicles.list({ page: 0, size: 100 }),
    queryFn: () => fetchVehicles({ page: 0, size: 100 }),
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

  const vehicles = toOptions(vehiclesQuery.data, (vehicle) => ({
    value: vehicle.id?.toString() ?? '',
    label: vehicle.gosNumber ? `${vehicle.gosNumber}` : `ТС #${vehicle.id}`
  }));

  const retailPoints = toOptions(retailPointsQuery.data, (point) => ({
    value: point.id?.toString() ?? '',
    label: point.name ?? `Точка #${point.id}`
  }));

  const orders = toOrderOptions(ordersQuery.data);

  const handleSubmit = async (values: RouteFormValues) => {
    const parsed = routeSchema.safeParse(values);
    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        form.setError(issue.path.join('.') as any, { message: issue.message });
      });
      return;
    }

    const payload = mapFormToPayload(parsed.data, initialRoute?.id);
    await onSubmit(payload);
  };

  return (
    <FormProvider {...form}>
      <form className="space-y-6" onSubmit={form.handleSubmit(handleSubmit)}>
        <Card>
          <CardHeader>
            <CardTitle>Основные сведения</CardTitle>
            <CardDescription>Заполните основные сведения о маршруте и точках посещения.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="vehicleId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Транспорт</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={vehicles.length === 0}>
                      <SelectTrigger>
                        <SelectValue placeholder="Выберите транспорт" />
                      </SelectTrigger>
                      <SelectContent>
                        {vehicles.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription>Для маршрута требуется выбрать транспортное средство.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="mileageInKm"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Пробег, км</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.001" min={0} placeholder="Например 125.750" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="creationTime"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Дата создания (UTC)</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="plannedStartTime"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Плановое начало (UTC)</FormLabel>
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
                    <FormLabel>Плановое окончание (UTC)</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Точки маршрута</h3>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() =>
                    append({
                      id: undefined,
                      retailPointId: '',
                      operationType: OperationType.LOAD,
                      orderIds: [],
                      plannedStartTime: formatDateTimeInput(undefined, true),
                      plannedEndTime: formatDateTimeInput(undefined, true),
                      orderNumber: pointFields.length,
                      routeId: initialRoute?.id ?? null
                    })
                  }
                >
                  Добавить точку
                </Button>
              </div>

              {pointFields.length === 0 ? (
                <p className="rounded-md border border-dashed border-border px-4 py-6 text-sm text-muted-foreground">
                  Добавьте хотя бы одну точку, чтобы маршрут был валидным.
                </p>
              ) : (
                <div className="space-y-4">
                  {pointFields.map((field, index) => (
                    <Card key={field.id} className="border-muted">
                      <CardHeader className="flex flex-row items-center justify-between gap-2">
                        <div>
                          <CardTitle>Точка маршрута #{index + 1}</CardTitle>
                          <CardDescription>
                            Укажите точку назначения, окно обслуживания и связанные заказы.
                          </CardDescription>
                        </div>
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          onClick={() => remove(index)}
                          disabled={pointFields.length === 1}
                        >
                          Удалить
                        </Button>
                      </CardHeader>
                      <CardContent className="grid grid-cols-1 gap-4 md:grid-cols-2">
                        <FormField
                          control={form.control}
                          name={`routePoints.${index}.retailPointId`}
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Торговая точка</FormLabel>
                              <Select onValueChange={field.onChange} value={field.value}>
                                <SelectTrigger>
                                  <SelectValue placeholder="Выберите точку" />
                                </SelectTrigger>
                                <SelectContent>
                                  {retailPoints.map((option) => (
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
                          name={`routePoints.${index}.operationType`}
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Тип операции</FormLabel>
                              <Select onValueChange={field.onChange} value={field.value}>
                                <SelectTrigger>
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  {Object.values(OperationType).map((type) => (
                                    <SelectItem key={type} value={type}>
                                      {operationTypeLabels[type]}
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
                          name={`routePoints.${index}.plannedStartTime`}
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Окно начала</FormLabel>
                              <FormControl>
                                <Input type="datetime-local" {...field} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />

                        <FormField
                          control={form.control}
                          name={`routePoints.${index}.plannedEndTime`}
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Окно завершения</FormLabel>
                              <FormControl>
                                <Input type="datetime-local" {...field} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />

                        <FormField
                          control={form.control}
                          name={`routePoints.${index}.orderNumber`}
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
                          name={`routePoints.${index}.orderIds`}
                          render={({ field }) => (
                            <FormItem className="md:col-span-2">
                              <FormLabel>Связанные заказы</FormLabel>
                              <OrdersPicker value={field.value ?? []} onChange={field.onChange} options={orders} />
                              <FormDescription>
                                Выберите один или несколько заказов, связанных с точкой.
                              </FormDescription>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Сохранение…' : initialRoute ? 'Сохранить изменения' : 'Создать маршрут'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}

function OrdersPicker({
  value,
  onChange,
  options
}: {
  value: string[];
  onChange: (next: string[]) => void;
  options: Option<string>[];
}) {
  const [pending, setPending] = useState<string>('');
  const selectedSet = new Set(value ?? []);
  const available = options.filter((o) => !selectedSet.has(o.value));
  const lookup = useMemo(() => new Map(options.map((o) => [o.value, o.label])), [options]);

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <Select value={pending} onValueChange={setPending}>
          <SelectTrigger className="w-72">
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

function mapRouteToForm(route?: Route): RouteFormValues {
  if (!route) {
    return {
      vehicleId: '',
      creationTime: formatDateTimeInput(),
      plannedStartTime: formatDateTimeInput(undefined, true),
      plannedEndTime: formatDateTimeInput(undefined, true),
      mileageInKm: '',
      routePoints: []
    };
  }

  return {
    vehicleId: route.vehicle?.id?.toString() ?? '',
    creationTime: formatDateTimeInput(route.creationTime),
    plannedStartTime: formatDateTimeInput(route.plannedStartTime, true),
    plannedEndTime: formatDateTimeInput(route.plannedEndTime, true),
    mileageInKm: route.mileageInKm?.toString() ?? '',
    routePoints: (route.routePoints ?? []).map((point) => mapRoutePointToForm(point, route.id ?? null))
  };
}

function mapRoutePointToForm(point: RoutePoint, routeId: number | null): RouteFormValues['routePoints'][number] {
  return {
    id: point.id ?? undefined,
    retailPointId: point.retailPoint?.id?.toString() ?? '',
    operationType: point.operationType ?? OperationType.VISIT,
    orderIds: (point.orders ?? []).map((order) => order.id?.toString() ?? '').filter(Boolean),
    plannedStartTime: formatDateTimeInput(point.plannedStartTime, true),
    plannedEndTime: formatDateTimeInput(point.plannedEndTime, true),
    orderNumber: point.orderNumber ?? 0,
    routeId
  };
}

function mapFormToPayload(values: RouteFormValues, routeId?: number): SaveRoute {
  return {
    vehicleId: Number(values.vehicleId),
    creationTime: toUtcDate(values.creationTime),
    plannedStartTime: toUtcDate(values.plannedStartTime),
    plannedEndTime: toUtcDate(values.plannedEndTime),
    mileageInKm: Number(values.mileageInKm),
    routePoints: values.routePoints.map((point, index) => ({
      id: point.id,
      routeId: point.routeId ?? routeId ?? undefined,
      retailPointId: Number(point.retailPointId),
      operationType: point.operationType,
      orderIds: parseOrderIds(point.orderIds),
      plannedStartTime: toUtcDate(point.plannedStartTime),
      plannedEndTime: toUtcDate(point.plannedEndTime),
      orderNumber: point.orderNumber ?? index
    }))
  };
}

function parseOrderIds(values: string[] | undefined): number[] {
  if (!values || values.length === 0) {
    return [];
  }
  return values.map((value) => Number(value)).filter((num) => !Number.isNaN(num));
}

function formatDateTimeInput(value?: Date | string | null, preferLocal = false): string {
  if (typeof value === 'string') {
    const trimmed = value.trim();
    const match = trimmed.match(/^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}/);
    if (match) {
      return match[0];
    }
    const parsed = new Date(normalizeToUtcIso(trimmed));
    if (!Number.isNaN(parsed.getTime())) {
      return preferLocal ? formatLocalDateTime(parsed) : formatUtcDateTime(parsed);
    }
    return '';
  }

  const date = value ? new Date(value) : new Date();
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return preferLocal ? formatLocalDateTime(date) : formatUtcDateTime(date);
}

function toUtcDate(value: string): Date {
  if (!value) {
    throw new Error('Дата не указана');
  }
  const date = new Date(normalizeToUtcIso(value));
  if (Number.isNaN(date.getTime())) {
    throw new Error(`Некорректная дата: ${value}`);
  }
  return date;
}

function normalizeToUtcIso(value: string): string {
  let normalized = value.trim();
  if (!normalized.includes('T')) {
    normalized = `${normalized}T00:00`;
  }
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(normalized)) {
    normalized = `${normalized}:00`;
  }
  if (!/(Z|z|[+-]\d{2}:\d{2})$/.test(normalized)) {
    normalized = `${normalized}Z`;
  }
  return normalized;
}

function formatLocalDateTime(date: Date): string {
  const pad = (num: number) => num.toString().padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function formatUtcDateTime(date: Date): string {
  return date.toISOString().slice(0, 16);
}

function toOrderOptions<T extends { id?: number | null; goodsType?: string | null }>(
  result: PageResult<T> | undefined
): Option<string>[] {
  if (!result) {
    return [];
  }
  return result.data
    .filter((item) => item.id != null)
    .map((item) => ({
      value: item.id!.toString(),
      label: `Заказ #${item.id} (${item.goodsType ?? '—'})`
    }));
}

function toOptions<T extends { id?: number | null }>(
  result: PageResult<T> | undefined,
  map: (item: T) => Option<string>
): Option<string>[] {
  if (!result) {
    return [];
  }
  return result.data.map(map).filter((option) => option.value !== '');
}
