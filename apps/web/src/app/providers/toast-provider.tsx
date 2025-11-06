import {
  Toast,
  ToastAction,
  ToastClose,
  ToastDescription,
  ToastProvider as RadixToastProvider,
  ToastTitle,
  ToastViewport
} from '@shared/ui/toast';
import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
  type PropsWithChildren
} from 'react';
import { cn } from '@shared/lib/cn';

type ToastVariant = 'default' | 'success' | 'error' | 'warning';

interface ToastOptions {
  title?: string;
  description?: string;
  duration?: number;
  actionLabel?: string;
  onAction?: () => void;
  variant?: ToastVariant;
}

interface ToastRecord extends ToastOptions {
  id: number;
}

interface ToastContextValue {
  push: (options: ToastOptions) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

function getVariantClasses(variant: ToastVariant = 'default') {
  switch (variant) {
    case 'success':
      return 'border-success/50 bg-success/10 text-foreground';
    case 'error':
      return 'border-destructive/50 bg-destructive/10 text-foreground';
    case 'warning':
      return 'border-warning/50 bg-warning/10 text-foreground';
    default:
      return 'border-border bg-card text-foreground';
  }
}

export function ToastProvider({ children }: PropsWithChildren) {
  const [toasts, setToasts] = useState<ToastRecord[]>([]);
  const idRef = useRef(0);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const push = useCallback((options: ToastOptions) => {
    setToasts((prev) => {
      const nextId = ++idRef.current;
      return [...prev, { id: nextId, ...options }];
    });
  }, []);

  const value = useMemo<ToastContextValue>(() => ({ push }), [push]);

  return (
    <ToastContext.Provider value={value}>
      <RadixToastProvider swipeDirection="right">
        {children}
        <ToastViewport />
        {toasts.map(({ id, title, description, actionLabel, onAction, duration, variant }) => (
          <Toast
            key={id}
            duration={duration ?? 5000}
            className={cn(getVariantClasses(variant))}
            onOpenChange={(open) => {
              if (!open) {
                removeToast(id);
              }
            }}
          >
            <div className="space-y-1">
              {title ? <ToastTitle>{title}</ToastTitle> : null}
              {description ? <ToastDescription>{description}</ToastDescription> : null}
            </div>
            <div className="flex items-center gap-2 self-start">
              {actionLabel ? (
                <ToastAction altText={actionLabel} onClick={onAction}>
                  {actionLabel}
                </ToastAction>
              ) : null}
              <ToastClose />
            </div>
          </Toast>
        ))}
      </RadixToastProvider>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast должен использоваться внутри ToastProvider');
  }
  return context;
}

export function useToastHelpers() {
  const { push } = useToast();

  const notifySuccess = useCallback(
    (message: string, description?: string) =>
      push({ title: message, description, variant: 'success' }),
    [push]
  );

  const notifyError = useCallback(
    (message: string, description?: string) =>
      push({ title: message, description, variant: 'error', duration: 8000 }),
    [push]
  );

  const notifyWarning = useCallback(
    (message: string, description?: string) =>
      push({ title: message, description, variant: 'warning' }),
    [push]
  );

  return { push, notifySuccess, notifyError, notifyWarning };
}
