export type TipoMovimiento = 'DEPOSITO' | 'RETIRO';

export interface Movimiento {
  id?: number;
  fecha?: string;
  tipoMovimiento: TipoMovimiento;
  valor: number;
  saldo?: number;
  numeroCuenta: string;
}

export interface ReporteItem {
  Fecha: string;
  Cliente: string;
  NumeroCuenta: string;
  Tipo: string;
  SaldoInicial: number;
  Estado: boolean;
  Movimiento: number;
  SaldoDisponible: number;
}

export interface ReporteResponse {
  formato: string;
  movimientos: ReporteItem[];
  pdfBase64?: string;
}
