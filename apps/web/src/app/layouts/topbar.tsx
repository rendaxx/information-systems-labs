import { ThemeToggle } from '@features/theme-toggle/theme-toggle';
import { useWebSocketStatus } from '@app/providers/websocket-provider';
import { cn } from '@shared/lib/cn';

export function Topbar() {
  const status = useWebSocketStatus();

  const statusConfig = {
    connected: {
      label: 'WS: подключено',
      indicator: 'bg-success'
    },
    connecting: {
      label: 'WS: подключение…',
      indicator: 'bg-warning'
    },
    disconnected: {
      label: 'WS: нет подключения',
      indicator: 'bg-destructive'
    }
  }[status];

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-card px-6">
      <div className="text-sm text-muted-foreground">
        Панель управления
      </div>
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 rounded-full border border-border bg-muted/50 px-3 py-1 text-xs font-medium text-muted-foreground">
          <span className="relative flex h-2 w-2">
            <span
              className={cn(
                'absolute inline-flex h-full w-full animate-ping rounded-full opacity-60',
                statusConfig.indicator
              )}
            ></span>
            <span className={cn('relative inline-flex h-2 w-2 rounded-full', statusConfig.indicator)}></span>
          </span>
          <span>{statusConfig.label}</span>
        </div>
        <ThemeToggle />
      </div>
    </header>
  );
}
