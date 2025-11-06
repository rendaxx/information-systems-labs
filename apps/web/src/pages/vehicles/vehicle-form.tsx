import { FormProvider, useForm } from 'react-hook-form';
import { z } from 'zod';
import type { SaveVehicle, Vehicle } from '@rendaxx/api-ts';
import { Button } from '@shared/ui/button';
import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/ui/form';
import { Input } from '@shared/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchDrivers } from '@entities/drivers/api/drivers-api';
import { queryKeys } from '@shared/api/query-keys';
import type { PageResult } from '@shared/api/types';

const schema = z.object({
  driverId: z.string().min(1, 'Выберите водителя'),
  gosNumber: z.string().min(1, 'Укажите госномер'),
  tonnageInTons: z.string().min(1, 'Укажите грузоподъёмность'),
  bodyHeightInMeters: z.string().min(1, 'Укажите высоту'),
  bodyWidthInMeters: z.string().min(1, 'Укажите ширину'),
  bodyLengthInCubicMeters: z.string().min(1, 'Укажите длину/объём')
});

type VehicleFormValues = z.infer<typeof schema>;

interface VehicleFormProps {
  initialVehicle?: Vehicle;
  onSubmit: (payload: SaveVehicle) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

interface Option {
  value: string;
  label: string;
}

export function VehicleForm({ initialVehicle, onSubmit, onCancel, isSubmitting }: VehicleFormProps) {
  const defaultValues = useMemo<VehicleFormValues>(() => {
    if (!initialVehicle) {
      return {
        driverId: '',
        gosNumber: '',
        tonnageInTons: '',
        bodyHeightInMeters: '',
        bodyWidthInMeters: '',
        bodyLengthInCubicMeters: ''
      };
    }
    return {
      driverId: initialVehicle.driver?.id?.toString() ?? '',
      gosNumber: initialVehicle.gosNumber ?? '',
      tonnageInTons: toInput(initialVehicle.tonnageInTons),
      bodyHeightInMeters: toInput(initialVehicle.bodyHeightInMeters),
      bodyWidthInMeters: toInput(initialVehicle.bodyWidthInMeters),
      bodyLengthInCubicMeters: toInput(initialVehicle.bodyLengthInCubicMeters)
    };
  }, [initialVehicle]);

  const form = useForm<VehicleFormValues>({
    defaultValues,
    mode: 'onBlur'
  });

  const driversQuery = useQuery({
    queryKey: queryKeys.drivers.list({ page: 0, size: 100 }),
    queryFn: () => fetchDrivers({ page: 0, size: 100 }),
    staleTime: 60_000
  });

  const driverOptions = toDriverOptions(driversQuery.data);

  const handleSubmit = async (values: VehicleFormValues) => {
    const parsed = schema.safeParse(values);
    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        form.setError(issue.path.join('.') as any, { message: issue.message });
      });
      return;
    }

    const payload: SaveVehicle = {
      driverId: Number(parsed.data.driverId),
      gosNumber: parsed.data.gosNumber,
      tonnageInTons: Number(parsed.data.tonnageInTons),
      bodyHeightInMeters: Number(parsed.data.bodyHeightInMeters),
      bodyWidthInMeters: Number(parsed.data.bodyWidthInMeters),
      bodyLengthInCubicMeters: Number(parsed.data.bodyLengthInCubicMeters)
    };

    await onSubmit(payload);
  };

  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>{initialVehicle ? 'Редактирование транспорта' : 'Новое транспортное средство'}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <FormField
              control={form.control}
              name="gosNumber"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Гос. номер</FormLabel>
                  <FormControl>
                    <Input placeholder="A123BC-7" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="driverId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Водитель</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value} disabled={driverOptions.length === 0}>
                    <SelectTrigger>
                      <SelectValue placeholder="Выберите водителя" />
                    </SelectTrigger>
                    <SelectContent>
                      {driverOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormDescription>Список подгружается с сервера (первые 100 записей).</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <FormField
                control={form.control}
                name="tonnageInTons"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Грузоподъёмность, т</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.1" min={0} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="bodyHeightInMeters"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Высота, м</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.01" min={0} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="bodyWidthInMeters"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Ширина, м</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.01" min={0} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="bodyLengthInCubicMeters"
                render={({ field }) => (
                  <FormItem className="md:col-span-3">
                    <FormLabel>Длина/объём кузова, м</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.01" min={0} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </CardContent>
        </Card>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Сохранение…' : initialVehicle ? 'Сохранить изменения' : 'Создать транспорт'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}

function toDriverOptions(result?: PageResult<any>): Option[] {
  if (!result) {
    return [];
  }
  return result.data
    .filter((driver) => driver.id != null)
    .map((driver) => ({
      value: String(driver.id),
      label: `${driver.lastName ?? ''} ${driver.firstName ?? ''} ${driver.middleName ?? ''}`.trim() || `ID ${driver.id}`
    }));
}

function toInput(value?: number | null) {
  return value != null ? Number(value).toFixed(2) : '';
}
