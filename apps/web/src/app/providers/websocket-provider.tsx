import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type PropsWithChildren
} from 'react';

type WebSocketStatus = 'connecting' | 'connected' | 'disconnected';

type MessageHandler = (message: IMessage) => void;

type SubscriptionEntry = {
  topic: string;
  handler: MessageHandler;
  subscription?: StompSubscription;
};

interface WebSocketContextValue {
  status: WebSocketStatus;
  subscribe: (topic: string, handler: MessageHandler) => () => void;
}

const WebSocketContext = createContext<WebSocketContextValue | undefined>(undefined);

function resolveWebSocketUrl(): string {
  const fallback = 'http://localhost:8080';
  const base = import.meta.env.VITE_API_BASE_URL ?? fallback;

  try {
    const baseUrl = new URL(base, window.location.origin);
    const protocol = baseUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${baseUrl.host}/ws`;
  } catch (error) {
    console.warn('Некорректный VITE_API_BASE_URL, использую localhost', error);
    return 'ws://localhost:8080/ws';
  }
}

export function WebSocketProvider({ children }: PropsWithChildren) {
  const [status, setStatus] = useState<WebSocketStatus>('connecting');
  const clientRef = useRef<Client | null>(null);
  const entriesRef = useRef<Set<SubscriptionEntry>>(new Set());

  useEffect(() => {
    const brokerURL = resolveWebSocketUrl();
    const debug = (msg: string) => {
      if (import.meta.env.DEV) {
        console.debug('[STOMP]', msg);
      }
    };

    const client = new Client({
      brokerURL,
      reconnectDelay: 5000,
      debug
    });

    client.onConnect = () => {
      setStatus('connected');
      entriesRef.current.forEach((entry) => {
        if (entry.subscription) {
          try {
            entry.subscription.unsubscribe();
          } catch (error) {
            console.warn('Ошибка при отписке от старой подписки', error);
          }
          entry.subscription = undefined;
        }
        attachSubscription(client, entry);
      });
    };

    client.onWebSocketClose = () => {
      setStatus('disconnected');
    };

    client.onStompError = (frame) => {
      console.error('STOMP error', frame.headers['message'], frame.body);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      setStatus('disconnected');
      client.deactivate();
      clientRef.current = null;
      entriesRef.current.forEach((entry) => {
        entry.subscription = undefined;
      });
      entriesRef.current.clear();
    };
  }, []);

  const subscribe = useCallback<WebSocketContextValue['subscribe']>((topic, handler) => {
    const entry: SubscriptionEntry = { topic, handler };
    entriesRef.current.add(entry);

    const client = clientRef.current;
    if (client?.connected) {
      attachSubscription(client, entry);
    }

    return () => {
      if (entry.subscription) {
        try {
          entry.subscription.unsubscribe();
        } catch (error) {
          console.warn('Не удалось отписаться от WebSocket', error);
        }
      }
      entriesRef.current.delete(entry);
    };
  }, []);

  const value = useMemo<WebSocketContextValue>(() => ({ status, subscribe }), [status, subscribe]);

  return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>;
}

function attachSubscription(client: Client, entry: SubscriptionEntry) {
  if (entry.subscription) {
    return;
  }

  entry.subscription = client.subscribe(entry.topic, (message) => {
    try {
      entry.handler(message);
    } catch (error) {
      console.error('Ошибка обработки сообщения WebSocket', error);
    }
  });
}

export function useWebSocketContext() {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocketContext должен использоваться внутри WebSocketProvider');
  }
  return context;
}

export function useWebSocketSubscription(topic: string, handler: MessageHandler, enabled = true) {
  const { subscribe } = useWebSocketContext();

  useEffect(() => {
    if (!enabled) {
      return;
    }
    const unsubscribe = subscribe(topic, handler);
    return () => unsubscribe();
  }, [topic, handler, enabled, subscribe]);
}

export function useWebSocketStatus(): WebSocketStatus {
  const { status } = useWebSocketContext();
  return status;
}
