import { FormProvider, useForm } from 'react-hook-form';
import { z } from 'zod';
import type { Order, SaveOrder } from '@rendaxx/api-ts';
import { Button } from '@shared/ui/button';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@shared/ui/form';
import { Input } from '@shared/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { useMemo } from 'react';

const numericField = (message: string) =>
  z
    .string()
    .min(1, message)
    .refine((value) => !Number.isNaN(Number(value)), 'Введите число');

const schema = z.object({
  goodsType: z.string().min(1, 'Укажите тип товара'),
  minTemperature: numericField('Укажите мин. температуру'),
  maxTemperature: numericField('Укажите макс. температуру'),
  volumeInCubicMeters: numericField('Укажите объём'),
  weightInKg: numericField('Укажите вес')
});

type OrderFormValues = z.infer<typeof schema>;

interface OrderFormProps {
  initialOrder?: Order;
  onSubmit: (payload: SaveOrder) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function OrderForm({ initialOrder, onSubmit, onCancel, isSubmitting }: OrderFormProps) {
  const defaultValues = useMemo<OrderFormValues>(() => {
    if (!initialOrder) {
      return {
        goodsType: '',
        minTemperature: '',
        maxTemperature: '',
        volumeInCubicMeters: '',
        weightInKg: ''
      };
    }
    return {
      goodsType: initialOrder.goodsType ?? '',
      minTemperature: toInput(initialOrder.minTemperature),
      maxTemperature: toInput(initialOrder.maxTemperature),
      volumeInCubicMeters: initialOrder.volumeInCubicMeters?.toString() ?? '',
      weightInKg: initialOrder.weightInKg?.toString() ?? ''
    };
  }, [initialOrder]);

  const form = useForm<OrderFormValues>({
    defaultValues,
    mode: 'onBlur'
  });

  const handleSubmit = async (values: OrderFormValues) => {
    const parsed = schema.safeParse(values);
    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        form.setError(issue.path.join('.') as any, { message: issue.message });
      });
      return;
    }

    const payload: SaveOrder = {
      goodsType: parsed.data.goodsType,
      minTemperature: Number(parsed.data.minTemperature),
      maxTemperature: Number(parsed.data.maxTemperature),
      volumeInCubicMeters: Number(parsed.data.volumeInCubicMeters),
      weightInKg: Number(parsed.data.weightInKg)
    };

    await onSubmit(payload);
  };

  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Основные сведения</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <FormField
              control={form.control}
              name="goodsType"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Тип товара</FormLabel>
                  <FormControl>
                    <Input placeholder="Например: молочная продукция" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="weightInKg"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Вес, кг</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.1" min={0} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="volumeInCubicMeters"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Объём, м³</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.01" min={0} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="minTemperature"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Мин. температура, °C</FormLabel>
                    <FormControl>
                      <Input type="number" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="maxTemperature"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Макс. температура, °C</FormLabel>
                    <FormControl>
                      <Input type="number" {...field} />
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
            {isSubmitting ? 'Сохранение…' : initialOrder ? 'Сохранить изменения' : 'Создать заказ'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}

function toInput(value: number | null | undefined): string {
  return value != null ? value.toString() : '';
}
