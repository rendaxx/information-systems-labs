import type { PropsWithChildren } from 'react';
import { ThemeProvider } from './theme-provider';
import { QueryProvider } from './query-provider';
import { ToastProvider } from './toast-provider';
import { TooltipProvider } from '@shared/ui/tooltip';
import { WebSocketProvider } from './websocket-provider';

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <ThemeProvider>
      <QueryProvider>
        <WebSocketProvider>
          <ToastProvider>
            <TooltipProvider delayDuration={150} skipDelayDuration={400} disableHoverableContent>
              {children}
            </TooltipProvider>
          </ToastProvider>
        </WebSocketProvider>
      </QueryProvider>
    </ThemeProvider>
  );
}
