import {
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable,
  type ColumnDef
} from '@tanstack/react-table';
import { type ReactNode } from 'react';
import type { PageMetadata } from '@shared/api/types';
import { cn } from '@shared/lib/cn';
import type { ServerTableHandlers, ServerTableState } from '@shared/lib/server-table';
import { Button } from '@shared/ui/button';
import { Skeleton } from '@shared/ui/skeleton';

export interface DataTableProps<TData> {
  data: TData[];
  columns: ColumnDef<TData, any>[];
  meta: PageMetadata;
  state: ServerTableState;
  handlers: Pick<ServerTableHandlers, 'onSortingChange' | 'onPageChange' | 'onSizeChange'>;
  isLoading?: boolean;
  isFetching?: boolean;
  emptyState?: ReactNode;
}

const sizes = [10, 20, 50, 100];

export function DataTable<TData>({ data, columns, meta, state, handlers, isLoading, isFetching, emptyState }: DataTableProps<TData>) {
  const table = useReactTable({
    data,
    columns,
    state: {
      sorting: state.sorting
    },
    manualPagination: true,
    manualSorting: true,
    onSortingChange: handlers.onSortingChange,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    pageCount: meta.totalPages ?? -1
  });

  const showEmpty = !isLoading && data.length === 0;

  return (
    <div className="space-y-4">
      <div className="overflow-hidden rounded-lg border border-border">
        <table className="w-full border-collapse text-sm">
          <thead className="bg-muted/60 text-left text-xs uppercase tracking-wide text-muted-foreground">
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => {
                  const sorted = header.column.getIsSorted();
                  return (
                    <th
                      key={header.id}
                      className={cn(
                        'border-b border-border px-4 py-3 font-semibold',
                        header.column.getCanSort() && 'cursor-pointer select-none'
                      )}
                      onClick={header.column.getToggleSortingHandler()}
                    >
                      <div className="flex items-center gap-2">
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {sorted ? (
                          <span className="text-muted-foreground">{sorted === 'desc' ? '↓' : '↑'}</span>
                        ) : null}
                      </div>
                    </th>
                  );
                })}
              </tr>
            ))}
          </thead>
          <tbody>
            {isLoading ? (
              [...Array(5).keys()].map((index) => (
                <tr key={index} className="border-b border-border">
                  <td className="px-4 py-3" colSpan={columns.length}>
                    <Skeleton className="h-6 w-full" />
                  </td>
                </tr>
              ))
            ) : showEmpty ? (
              <tr>
                <td colSpan={columns.length} className="px-4 py-10 text-center text-muted-foreground">
                  {emptyState ?? 'Нет данных для отображения'}
                </td>
              </tr>
            ) : (
              table.getRowModel().rows.map((row) => (
                <tr key={row.id} className="border-b border-border last:border-b-0">
                  {row.getVisibleCells().map((cell) => (
                    <td key={cell.id} className="px-4 py-3">
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            Страница {Math.min(state.page + 1, Math.max(meta.totalPages, 1))} из {Math.max(meta.totalPages, 1)} · Всего записей: {meta.totalElements}
          {isFetching ? <span className="ml-2 text-xs text-muted-foreground">Обновление…</span> : null}
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Размер</span>
            <select
              className="h-9 rounded-md border border-input bg-background px-2 text-sm"
              value={state.size}
              onChange={(event) => handlers.onSizeChange(Number(event.target.value))}
            >
              {sizes.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-center gap-2">
            <Button
              type="button"
              variant="ghost"
              size="sm"
              disabled={state.page === 0}
              onClick={() => handlers.onPageChange(0)}
            >
              « Первая
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              disabled={state.page === 0}
              onClick={() => handlers.onPageChange(Math.max(state.page - 1, 0))}
            >
              ‹ Назад
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              disabled={state.page >= meta.totalPages - 1 || meta.totalPages === 0}
              onClick={() => handlers.onPageChange(Math.min(state.page + 1, Math.max(meta.totalPages - 1, 0)))}
            >
              Вперёд ›
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              disabled={state.page >= meta.totalPages - 1 || meta.totalPages === 0}
              onClick={() => handlers.onPageChange(Math.max(meta.totalPages - 1, 0))}
            >
              Последняя »
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
