import type { SortingState } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import type { PageRequest, SortOption } from '@shared/api/types';

export interface ServerTableState {
  page: number;
  size: number;
  sorting: SortingState;
  filters: Record<string, string>;
}

export interface ServerTableHandlers {
  onSortingChange: (updater: SortingState | ((state: SortingState) => SortingState)) => void;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
  onFilterChange: (key: string, value: string) => void;
  resetFilters: () => void;
}

export function useServerTable(initial?: Partial<ServerTableState>) {
  const [page, setPage] = useState(initial?.page ?? 0);
  const [size, setSize] = useState(initial?.size ?? 20);
  const [sorting, setSorting] = useState<SortingState>(initial?.sorting ?? []);
  const [filters, setFilters] = useState<Record<string, string>>(initial?.filters ?? {});

  const handlers: ServerTableHandlers = useMemo(
    () => ({
      onSortingChange: (updater) => {
        setSorting((current) => (typeof updater === 'function' ? updater(current) : updater));
      },
      onPageChange: setPage,
      onSizeChange: (value) => {
        setSize(value);
        setPage(0);
      },
      onFilterChange: (key, value) => {
        setFilters((prev) => {
          if (!key) {
            return prev;
          }
          const next = { ...prev };
          if (!value) {
            delete next[key];
          } else {
            next[key] = value;
          }
          return next;
        });
        setPage(0);
      },
      resetFilters: () => {
        setFilters({});
        setPage(0);
      }
    }),
    []
  );

  const request: PageRequest = useMemo(
    () => ({
      page,
      size,
      sort: sortingStateToSortOptions(sorting),
      filter: filters
    }),
    [filters, page, size, sorting]
  );

  return { state: { page, size, sorting, filters }, handlers, request };
}

export function sortingStateToSortOptions(state: SortingState): SortOption[] {
  return state.map(({ id, desc }) => ({ field: id, order: desc ? 'desc' : 'asc' }));
}

export function sortOptionsToSortingState(options?: SortOption[]): SortingState {
  if (!options) {
    return [];
  }
  return options.map((option) => ({ id: option.field, desc: option.order === 'desc' }));
}
