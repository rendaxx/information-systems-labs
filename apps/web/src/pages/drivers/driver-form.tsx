import { FormProvider, useForm } from 'react-hook-form';
import { z } from 'zod';
import type { Driver, SaveDriver } from '@rendaxx/api-ts';
import { Button } from '@shared/ui/button';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@shared/ui/form';
import { Input } from '@shared/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { useMemo } from 'react';

const schema = z.object({
  firstName: z.string().min(1, 'Введите имя'),
  middleName: z.string().optional(),
  lastName: z.string().min(1, 'Введите фамилию'),
  passport: z.string().min(1, 'Укажите паспортные данные')
});

type DriverFormValues = z.infer<typeof schema>;

interface DriverFormProps {
  initialDriver?: Driver;
  onSubmit: (payload: SaveDriver) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export function DriverForm({ initialDriver, onSubmit, onCancel, isSubmitting }: DriverFormProps) {
  const defaultValues = useMemo<DriverFormValues>(() => {
    if (!initialDriver) {
      return {
        firstName: '',
        middleName: '',
        lastName: '',
        passport: ''
      };
    }
    return {
      firstName: initialDriver.firstName ?? '',
      middleName: initialDriver.middleName ?? '',
      lastName: initialDriver.lastName ?? '',
      passport: initialDriver.passport ?? ''
    };
  }, [initialDriver]);

  const form = useForm<DriverFormValues>({
    defaultValues,
    mode: 'onBlur'
  });

  const handleSubmit = async (values: DriverFormValues) => {
    const parsed = schema.safeParse(values);
    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        form.setError(issue.path.join('.') as any, { message: issue.message });
      });
      return;
    }

    const payload: SaveDriver = {
      firstName: parsed.data.firstName,
      middleName: parsed.data.middleName ?? null,
      lastName: parsed.data.lastName,
      passport: parsed.data.passport
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
              name="lastName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Фамилия</FormLabel>
                  <FormControl>
                    <Input placeholder="Иванов" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="firstName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Имя</FormLabel>
                  <FormControl>
                    <Input placeholder="Иван" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="middleName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Отчество</FormLabel>
                  <FormControl>
                    <Input placeholder="Иванович" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="passport"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Паспортные данные</FormLabel>
                  <FormControl>
                    <Input placeholder="серия и номер" {...field} />
                  </FormControl>
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
            {isSubmitting ? 'Сохранение…' : initialDriver ? 'Сохранить изменения' : 'Создать водителя'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}
