import { ResponseError } from '@rendaxx/api-ts';

export interface ApiErrorDetails {
  status?: number;
  message: string;
  raw?: unknown;
}

export async function parseApiError(error: unknown): Promise<ApiErrorDetails> {
  if (error instanceof ResponseError) {
    const status = error.response.status;
    let message = error.message || `Ошибка ${status}`;
    try {
      const contentType = error.response.headers.get('content-type');
      if (contentType?.includes('application/json')) {
        const json = await error.response.json();
        message = typeof json === 'string' ? json : JSON.stringify(json);
      } else {
        const text = await error.response.text();
        if (text) {
          message = text;
        }
      }
    } catch (readError) {
      console.error('Не удалось разобрать ответ об ошибке', readError);
    }
    return { status, message, raw: error };
  }

  if (error instanceof Error) {
    return { message: error.message, raw: error };
  }

  return { message: 'Неизвестная ошибка', raw: error };
}
