import { FormProvider, useForm } from 'react-hook-form';
import { z } from 'zod';
import { PointType, type RetailPoint, type SaveRetailPoint } from '@rendaxx/api-ts';
import { Button } from '@shared/ui/button';
import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/ui/form';
import { Input } from '@shared/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { useMemo } from 'react';

const TIMEZONES = [
  'UTC',
  'Europe/Kaliningrad',
  'Europe/Moscow',
  'Europe/Samara',
  'Asia/Yekaterinburg',
  'Asia/Omsk',
  'Asia/Novosibirsk',
  'Asia/Krasnoyarsk',
  'Asia/Irkutsk',
  'Asia/Yakutsk',
  'Asia/Vladivostok',
  'Asia/Sakhalin',
  'Asia/Magadan',
  'Asia/Kamchatka'
];

const schema = z.object({
  name: z.string().min(1, 'Введите название'),
  address: z.string().min(1, 'Введите адрес'),
  latitude: z.string().min(1, 'Введите широту'),
  longitude: z.string().min(1, 'Введите долготу'),
  type: z.nativeEnum(PointType, { errorMap: () => ({ message: 'Выберите тип точки' }) }),
  timezone: z.string().min(1, 'Укажите часовой пояс')
});

type RetailPointFormValues = z.input<typeof schema>;

interface RetailPointFormProps {
  initialRetailPoint?: RetailPoint;
  onSubmit: (payload: SaveRetailPoint) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function RetailPointForm({ initialRetailPoint, onSubmit, onCancel, isSubmitting }: RetailPointFormProps) {
  const defaultValues = useMemo<RetailPointFormValues>(() => {
    if (!initialRetailPoint) {
      return {
        name: '',
        address: '',
        latitude: '',
        longitude: '',
        type: PointType.SHOP,
        timezone: 'UTC'
      };
    }
    const fallbackTimezone = initialRetailPoint.timezone ?? 'UTC';
    return {
      name: initialRetailPoint.name ?? '',
      address: initialRetailPoint.address ?? '',
      latitude: initialRetailPoint.location?.latitude?.toString() ?? '',
      longitude: initialRetailPoint.location?.longitude?.toString() ?? '',
      type: (initialRetailPoint.type as PointType) ?? PointType.SHOP,
      timezone: fallbackTimezone
    };
  }, [initialRetailPoint]);

  const form = useForm<RetailPointFormValues>({
    defaultValues,
    mode: 'onBlur'
  });

  const handleSubmit = async (values: RetailPointFormValues) => {
    const parsed = schema.safeParse(values);
    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        form.setError(issue.path.join('.') as any, { message: issue.message });
      });
      return;
    }

    const payload: SaveRetailPoint = {
      name: parsed.data.name,
      address: parsed.data.address,
      location: {
        latitude: Number(parsed.data.latitude),
        longitude: Number(parsed.data.longitude)
      },
      type: parsed.data.type,
      timezone: parsed.data.timezone
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
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Название</FormLabel>
                  <FormControl>
                    <Input placeholder="Например: Магазин №12" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="address"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Адрес</FormLabel>
                  <FormControl>
                    <Input placeholder="Город, улица, дом" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="latitude"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Широта</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.000001" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="longitude"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Долгота</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.000001" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="type"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Тип точки</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.entries(pointTypeLabels).map(([value, label]) => (
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

              <FormField
                control={form.control}
                name="timezone"
                render={({ field }) => {
                  const options = field.value && !TIMEZONES.includes(field.value)
                    ? [field.value, ...TIMEZONES]
                    : TIMEZONES;
                  return (
                    <FormItem>
                      <FormLabel>Часовой пояс</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <SelectTrigger>
                          <SelectValue placeholder="Выберите часовой пояс" />
                        </SelectTrigger>
                        <SelectContent className="max-h-64">
                          {options.map((zone) => (
                            <SelectItem key={zone} value={zone}>
                              {zone}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormDescription>Используйте идентификатор таймзоны IANA.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  );
                }}
              />
            </div>
          </CardContent>
        </Card>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Сохранение…' : initialRetailPoint ? 'Сохранить изменения' : 'Создать точку'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}

const pointTypeLabels: Record<PointType, string> = {
  [PointType.SHOP]: 'Магазин',
  [PointType.WAREHOUSE]: 'Склад',
  [PointType.GARAGE]: 'Гараж'
};
