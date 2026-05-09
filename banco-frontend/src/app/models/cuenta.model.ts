export interface Cuenta {
  id?: number;
  numeroCuenta: string;
  tipoCuenta: string;
  saldoInicial: number;
  estado?: boolean;
  clienteId: string;
  nombreCliente?: string;
}
