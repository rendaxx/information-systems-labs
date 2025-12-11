import * as PopoverPrimitive from '@radix-ui/react-popover';
import { useMemo } from 'react';
import { Button } from '@shared/ui/button';
import { Input } from '@shared/ui/input';
import { Label } from '@shared/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';

export type FilterFieldType = 'text' | 'number' | 'datetime' | 'select';

export interface FilterFieldOption {
  label: string;
  value: string;
}

export interface FilterField {
  key: string;
  label: string;
  type?: FilterFieldType;
  placeholder?: string;
  options?: FilterFieldOption[];
}

interface FilterPopoverProps {
  fields: FilterField[];
  values: Record<string, string>;
  onFilterChange: (key: string, value: string) => void;
  onReset: () => void;
}

export function FilterPopover({ fields, values, onFilterChange, onReset }: FilterPopoverProps) {
  const activeCount = useMemo(
    () => fields.filter((field) => Boolean(values[field.key])).length,
    [fields, values]
  );

  return (
    <div className="flex flex-wrap items-center gap-2">
      <PopoverPrimitive.Root modal={false}>
        <PopoverPrimitive.Trigger asChild>
          <Button type="button" variant="outline">
            Фильтры{activeCount ? ` (${activeCount})` : ''}
          </Button>
        </PopoverPrimitive.Trigger>
        <PopoverPrimitive.Portal>
          <PopoverPrimitive.Content
            sideOffset={8}
            collisionPadding={24}
            className="z-50 w-80 rounded-lg border border-border bg-card p-4 shadow-xl outline-none overflow-visible data-[side=top]:animate-slide-down data-[side=bottom]:animate-slide-up"
          >
            <div className="flex max-h-[60vh] flex-col gap-4">
              <div className="flex-1 space-y-4 overflow-y-auto pr-1 no-scrollbar">
                {fields.map((field) => (
                  <div key={field.key} className="space-y-2 px-1.5">
                    <Label className="text-xs text-muted-foreground" htmlFor={`filter-${field.key}`}>
                      {field.label}
                    </Label>
                    {renderField(field, values[field.key] ?? '', (value) => onFilterChange(field.key, value))}
                  </div>
                ))}
              </div>
              <div className="flex items-center justify-between border-t border-border pt-3">
                <Button type="button" variant="ghost" onClick={onReset} disabled={activeCount === 0}>
                  Очистить
                </Button>
                <PopoverPrimitive.Close asChild>
                  <Button type="button" variant="secondary">
                    Готово
                  </Button>
                </PopoverPrimitive.Close>
              </div>
            </div>
          </PopoverPrimitive.Content>
        </PopoverPrimitive.Portal>
      </PopoverPrimitive.Root>
      <Button type="button" variant="ghost" onClick={onReset} disabled={activeCount === 0}>
        Сбросить фильтры
      </Button>
    </div>
  );
}

function renderField(field: FilterField, rawValue: string, onChange: (value: string) => void) {
  const type = field.type ?? 'text';
  if (type === 'select' && field.options) {
    const handleSelectChange = (value: string) => {
      if (value === '__clear__') {
        onChange('');
        return;
      }
      onChange(value);
    };
    return (
      <Select value={rawValue || undefined} onValueChange={handleSelectChange}>
        <SelectTrigger>
          <SelectValue placeholder={field.placeholder ?? 'Выберите значение'} />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="__clear__">Очистить</SelectItem>
          {field.options.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    );
  }

  if (type === 'datetime') {
    const inputValue = toDateTimeInput(rawValue);
    return (
      <Input
        id={`filter-${field.key}`}
        type="datetime-local"
        value={inputValue}
        onChange={(event) => onChange(normalizeDateTime(event.target.value))}
        placeholder={field.placeholder}
      />
    );
  }

  const inputType = type === 'number' ? 'number' : 'text';
  return (
    <Input
      id={`filter-${field.key}`}
      type={inputType}
      value={rawValue}
      onChange={(event) => onChange(event.target.value)}
      placeholder={field.placeholder}
    />
  );
}

function toDateTimeInput(value: string): string {
  if (!value) {
    return '';
  }
  if (value.length >= 16) {
    return value.slice(0, 16);
  }
  return value;
}

function normalizeDateTime(value: string): string {
  if (!value) {
    return '';
  }
  return value.length === 16 ? `${value}:00` : value;
}
