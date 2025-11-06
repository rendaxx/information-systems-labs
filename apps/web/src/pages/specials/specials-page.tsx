import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchAverageMileage, fetchRoutesByRetailPoint, fetchRoutesWithinPeriod } from '@entities/routes/api/routes-api';
import { fetchRetailPoints } from '@entities/retail-points/api/retail-points-api';
import { queryKeys } from '@shared/api/query-keys';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/ui/card';
import { Input } from '@shared/ui/input';
import { Button } from '@shared/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/ui/select';
import { ResponsiveContainer, AreaChart, Area, CartesianGrid, Tooltip, XAxis, YAxis } from 'recharts';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';

export function SpecialsPage() {
  const [periodStart, setPeriodStart] = useState<string>('');
  const [periodEnd, setPeriodEnd] = useState<string>('');
  const [retailPointId, setRetailPointId] = useState<string>('');

  const avgMileageQuery = useQuery({
    queryKey: ['routes', 'averageMileage'],
    queryFn: fetchAverageMileage,
    staleTime: 60_000
  });

  const routesWithinPeriodQuery = useQuery({
    queryKey: ['routes', 'period', periodStart, periodEnd],
    queryFn: () => fetchRoutesWithinPeriod(new Date(periodStart).toISOString(), new Date(periodEnd).toISOString()),
    enabled: Boolean(periodStart) && Boolean(periodEnd),
    staleTime: 5_000
  });

  const retailPointsQuery = useQuery({
    queryKey: queryKeys.retailPoints.list({ page: 0, size: 100 }),
    queryFn: () => fetchRetailPoints({ page: 0, size: 100 }),
    staleTime: 60_000
  });

  const routesByRetailPointQuery = useQuery({
    queryKey: ['routes', 'byRetailPoint', retailPointId],
    queryFn: () => fetchRoutesByRetailPoint(Number(retailPointId)),
    enabled: Boolean(retailPointId),
    staleTime: 5_000
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Спецоперации</h1>
        <p className="text-muted-foreground">
          Анализируйте ключевые показатели маршрутов: средний пробег, активность в периодах и пересечения с торговыми точками.
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Средний пробег по всем маршрутам</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-semibold">
              {avgMileageQuery.isLoading ? 'Загрузка…' : `${avgMileageQuery.data?.toFixed(3) ?? '0.000'} км`}
            </div>
            <p className="mt-2 text-sm text-muted-foreground">
              Значение пересчитывается на основе всех маршрутов в системе.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Маршруты, начинающиеся и заканчивающиеся в период</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="text-xs font-medium text-muted-foreground" htmlFor="period-start">
                  Начало периода (UTC)
                </label>
                <Input id="period-start" type="datetime-local" value={periodStart} onChange={(event) => setPeriodStart(event.target.value)} />
              </div>
              <div>
                <label className="text-xs font-medium text-muted-foreground" htmlFor="period-end">
                  Конец периода (UTC)
                </label>
                <Input id="period-end" type="datetime-local" value={periodEnd} onChange={(event) => setPeriodEnd(event.target.value)} />
              </div>
            </div>
            <Button
              type="button"
              disabled={!periodStart || !periodEnd}
              onClick={() => routesWithinPeriodQuery.refetch()}
            >
              Обновить
            </Button>

            <div className="h-64">
              {routesWithinPeriodQuery.isLoading ? (
                <p className="text-sm text-muted-foreground">Загрузка…</p>
              ) : routesWithinPeriodQuery.data && routesWithinPeriodQuery.data.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart
                    data={routesWithinPeriodQuery.data.map((route) => ({
                      id: route.id,
                      start: new Date(route.plannedStartTime ?? new Date()).getTime(),
                      end: new Date(route.plannedEndTime ?? new Date()).getTime()
                    }))}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="start"
                      tickFormatter={(value) => format(value, 'd MMM HH:mm', { locale: ru })}
                      type="number"
                      domain={['dataMin', 'dataMax']}
                    />
                    <YAxis hide />
                    <Tooltip
                      labelFormatter={(value) => format(value, 'd MMM yyyy HH:mm', { locale: ru })}
                      formatter={(value: number, name) => {
                        if (name === 'end') {
                          return [format(value, 'd MMM yyyy HH:mm', { locale: ru }), 'Окончание'];
                        }
                        return [format(value, 'd MMM yyyy HH:mm', { locale: ru }), 'Начало'];
                      }}
                    />
                    <Area type="monotone" dataKey="start" stroke="#2563eb" fill="#60a5fa" name="Начало" />
                    <Area type="monotone" dataKey="end" stroke="#16a34a" fill="#bbf7d0" name="Окончание" />
                  </AreaChart>
                </ResponsiveContainer>
              ) : (
                <p className="text-sm text-muted-foreground">Нет маршрутов в указанном периоде</p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Маршруты по выбранной торговой точке</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-wrap gap-3">
              <Select value={retailPointId} onValueChange={setRetailPointId}>
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Выберите торговую точку" />
                </SelectTrigger>
                <SelectContent>
                  {retailPointsQuery.data?.data.map((point) => (
                    <SelectItem key={point.id} value={String(point.id)}>
                      {point.name ?? `Точка #${point.id}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button type="button" disabled={!retailPointId} onClick={() => routesByRetailPointQuery.refetch()}>
                Обновить
              </Button>
            </div>
            <ul className="space-y-2 text-sm">
              {routesByRetailPointQuery.isLoading ? (
                <li className="text-muted-foreground">Загрузка…</li>
              ) : routesByRetailPointQuery.data && routesByRetailPointQuery.data.length > 0 ? (
                routesByRetailPointQuery.data.map((route) => (
                  <li key={route.id} className="rounded-md border border-border px-3 py-2">
                    <div className="font-medium">Маршрут #{route.id}</div>
                    <div className="text-xs text-muted-foreground">
                      Окно: {formatDateTime(route.plannedStartTime)} — {formatDateTime(route.plannedEndTime)}
                    </div>
                  </li>
                ))
              ) : (
                <li className="text-muted-foreground">Маршруты не найдены</li>
              )}
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Топ посещаемых торговых точек</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm text-muted-foreground">
              Смотрите раздел «Торговые точки», где отображается рейтинг посещаемости и ближайшие точки.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function formatDateTime(value?: Date | string | null) {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }
  return format(date, 'd MMM yyyy HH:mm', { locale: ru });
}
