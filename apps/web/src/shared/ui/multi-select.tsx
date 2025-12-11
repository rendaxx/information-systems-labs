import * as Popover from '@radix-ui/react-popover';
import { ChevronDown } from 'lucide-react';
import { useMemo } from 'react';
import { Checkbox } from '@shared/ui/checkbox';
import { cn } from '@shared/lib/cn';

export interface MultiSelectOption {
  value: string;
  label: string;
}

export interface MultiSelectProps {
  value: string[];
  onChange: (next: string[]) => void;
  options: MultiSelectOption[];
  placeholder?: string;
  className?: string;
}

export function MultiSelect({ value, onChange, options, placeholder = 'Выберите…', className }: MultiSelectProps) {
  const summary = useMemo(() => {
    if (!value || value.length === 0) return placeholder;
    const labels = options.filter((o) => value.includes(o.value)).map((o) => o.label);
    return labels.length <= 2 ? labels.join(', ') : `${labels.slice(0, 2).join(', ')} +${labels.length - 2}`;
  }, [options, placeholder, value]);

  const toggle = (v: string, checked: boolean | string) => {
    const isChecked = checked === true;
    if (isChecked) {
      onChange([...new Set([...(value ?? []), v])]);
    } else {
      onChange((value ?? []).filter((x) => x !== v));
    }
  };

  return (
    <Popover.Root>
      <Popover.Trigger asChild>
        <button
          type="button"
          className={cn(
            'flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-left text-sm',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
            'disabled:cursor-not-allowed disabled:opacity-50',
            className
          )}
        >
          <span className={cn(value?.length ? 'text-foreground' : 'text-muted-foreground')}>{summary}</span>
          <ChevronDown className="h-4 w-4 opacity-60" />
        </button>
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content
          className={cn(
            'z-50 w-[var(--radix-popover-trigger-width)] rounded-md border border-border bg-card p-2 shadow-md',
            'max-h-64 overflow-auto'
          )}
          sideOffset={6}
        >
          {options.length === 0 ? (
            <div className="px-2 py-1 text-sm text-muted-foreground">Нет вариантов</div>
          ) : (
            <ul className="space-y-1">
              {options.map((option) => {
                const checked = value?.includes(option.value) ?? false;
                return (
                  <li key={option.value}>
                    <label className="flex cursor-pointer items-center gap-2 rounded px-2 py-1 text-sm hover:bg-muted">
                      <Checkbox checked={checked} onCheckedChange={(c) => toggle(option.value, c)} />
                      <span>{option.label}</span>
                    </label>
                  </li>
                );
              })}
            </ul>
          )}
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
}

