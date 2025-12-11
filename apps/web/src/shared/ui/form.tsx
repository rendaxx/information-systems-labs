import {
  createContext,
  useContext,
  type ComponentProps,
  type HTMLAttributes,
  type ReactNode
} from 'react';
import {
  Controller,
  FormProvider,
  useFormContext,
  type ControllerProps,
  type FieldPath,
  type FieldValues,
  type UseFormReturn
} from 'react-hook-form';
import { cn } from '@shared/lib/cn';
import { Label } from './label';

const FormFieldContext = createContext<{ name: string } | undefined>(undefined);
const FormItemContext = createContext<{ id: string } | undefined>(undefined);

export interface FormProps<TFieldValues extends FieldValues> {
  form: UseFormReturn<TFieldValues>;
  onSubmit: (values: TFieldValues) => void | Promise<void>;
  children: ReactNode;
  className?: string;
}

export function Form<TFieldValues extends FieldValues>({ form, onSubmit, children, className }: FormProps<TFieldValues>) {
  return (
    <FormProvider {...form}>
      <form className={className} onSubmit={form.handleSubmit(onSubmit)}>{children}</form>
    </FormProvider>
  );
}

export type FormFieldProps<TFieldValues extends FieldValues, TName extends FieldPath<TFieldValues>> = {
  name: TName;
} & Omit<ControllerProps<TFieldValues, TName>, 'render' | 'name'> & {
    render: ControllerProps<TFieldValues, TName>['render'];
  };

export function FormField<TFieldValues extends FieldValues, TName extends FieldPath<TFieldValues>>({
  name,
  render,
  ...props
}: FormFieldProps<TFieldValues, TName>) {
  return (
    <FormFieldContext.Provider value={{ name }}>
      <Controller name={name} render={render} {...props} />
    </FormFieldContext.Provider>
  );
}

export interface FormItemProps extends HTMLAttributes<HTMLDivElement> {}

export function FormItem({ className, ...props }: FormItemProps) {
  const fieldContext = useContext(FormFieldContext);
  const id = fieldContext ? `${fieldContext.name}-field` : undefined;
  return (
    <FormItemContext.Provider value={{ id: id ?? '' }}>
      <div className={cn('space-y-2', className)} {...props} />
    </FormItemContext.Provider>
  );
}

export function FormLabel({ className, ...props }: ComponentProps<typeof Label>) {
  const itemContext = useContext(FormItemContext);
  return <Label className={cn('text-sm font-medium', className)} htmlFor={itemContext?.id} {...props} />;
}

export function FormControl({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  const itemContext = useContext(FormItemContext);
  return <div id={itemContext?.id} className={cn('space-y-1', className)} {...props} />;
}

export function FormDescription({ className, ...props }: HTMLAttributes<HTMLParagraphElement>) {
  return <p className={cn('text-xs text-muted-foreground', className)} {...props} />;
}

export function FormMessage({ className, children, ...props }: HTMLAttributes<HTMLParagraphElement>) {
  const { getFieldState, formState } = useFormContext();
  const fieldContext = useContext(FormFieldContext);
  const fieldState = fieldContext ? getFieldState(fieldContext.name as FieldPath<FieldValues>, formState) : undefined;
  const message = typeof fieldState?.error?.message === 'string' ? fieldState.error.message : children;

  if (!message) {
    return null;
  }

  return (
    <p className={cn('text-sm text-destructive', className)} {...props}>
      {message}
    </p>
  );
}
