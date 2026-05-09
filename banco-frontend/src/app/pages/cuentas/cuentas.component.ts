import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Cuenta } from '../../models/cuenta.model';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';
import { MovimientoService } from '../../services/movimiento.service';
import { Movimiento } from '../../models/movimiento.model';

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './cuentas.component.html'
})
export class CuentasComponent implements OnInit {

  cuentas: Cuenta[] = [];
  clientes: Cliente[] = [];
  movimientos: Movimiento[] = [];
  filtro = '';
  errorMsg = '';
  successMsg = '';

  showForm = false;
  editando: Cuenta | null = null;
  form!: FormGroup;

  constructor(private cuentaService: CuentaService,
              private clienteService: ClienteService,
              private movService: MovimientoService,
              private fb: FormBuilder) {}

  ngOnInit(): void {
    this.buildForm();
    this.cargar();
    this.clienteService.listar().subscribe(c => this.clientes = c);
    this.movService.listar().subscribe(m => this.movimientos = m);
  }

  private buildForm(): void {
    this.form = this.fb.group({
      numeroCuenta: ['', [Validators.required]],
      tipoCuenta: ['Ahorros', [Validators.required]],
      saldoInicial: [0, [Validators.required, Validators.min(0)]],
      estado: [true],
      clienteId: ['', [Validators.required]]
    });
  }

  cargar(): void {
    this.cuentaService.listar().subscribe({
      next: (data) => this.cuentas = data,
      error: (err) => this.errorMsg = err.error?.message || 'Error al cargar cuentas'
    });
  }

  get cuentasFiltradas(): Cuenta[] {
    const q = this.filtro.trim().toLowerCase();
    if (!q) return this.cuentas;
    return this.cuentas.filter(c =>
      c.numeroCuenta.toLowerCase().includes(q) ||
      c.tipoCuenta.toLowerCase().includes(q) ||
      (c.nombreCliente || '').toLowerCase().includes(q) ||
      c.clienteId.toLowerCase().includes(q)
    );
  }

  abrirNuevo(): void {
    this.editando = null;
    this.form.reset({ tipoCuenta: 'Ahorros', estado: true, saldoInicial: 0 });
    this.showForm = true;
    this.clearMessages();
  }

  abrirEditar(c: Cuenta): void {
    this.editando = c;
    this.form.patchValue({
      numeroCuenta: c.numeroCuenta,
      tipoCuenta: c.tipoCuenta,
      saldoInicial: c.saldoInicial,
      estado: c.estado,
      clienteId: c.clienteId
    });
    this.showForm = true;
    this.clearMessages();
  }

  cerrarForm(): void {
    this.showForm = false;
    this.editando = null;
  }

  // Cierra el modal con tecla ESC
  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.showForm) {
      this.cerrarForm();
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const data: Cuenta = this.form.value;
    const obs = this.editando && this.editando.id
      ? this.cuentaService.actualizar(this.editando.id, data)
      : this.cuentaService.crear(data);

    obs.subscribe({
      next: () => {
        this.successMsg = this.editando ? 'Cuenta actualizada' : 'Cuenta creada';
        this.cerrarForm();
        this.cargar();
      },
      error: (err) => this.errorMsg = err.error?.message || 'Error al guardar'
    });
  }

  eliminar(c: Cuenta): void {
    if (!c.id) return;
    if (!confirm('Eliminar cuenta ' + c.numeroCuenta + '?')) return;
    this.cuentaService.eliminar(c.id).subscribe({
      next: () => {
        this.successMsg = 'Cuenta eliminada';
        this.cargar();
      },
      error: (err) => this.errorMsg = err.error?.message || 'Error al eliminar'
    });
  }

  // Calcula el saldo actual de la cuenta usando el ultimo movimiento
  saldoActualDe(numeroCuenta: string): number {
    const cuenta = this.cuentas.find(c => c.numeroCuenta === numeroCuenta);
    if (!cuenta) return 0;
    const movs = this.movimientos.filter(m => m.numeroCuenta === numeroCuenta);
    if (movs.length === 0) return cuenta.saldoInicial;
    const ultimo = movs[movs.length - 1];
    return ultimo.saldo ?? cuenta.saldoInicial;
  }

  esInvalido(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.dirty || c.touched);
  }

  private clearMessages(): void {
    this.errorMsg = '';
    this.successMsg = '';
  }
}
