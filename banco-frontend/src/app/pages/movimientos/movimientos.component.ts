import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Movimiento } from '../../models/movimiento.model';
import { MovimientoService } from '../../services/movimiento.service';
import { CuentaService } from '../../services/cuenta.service';
import { Cuenta } from '../../models/cuenta.model';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './movimientos.component.html'
})
export class MovimientosComponent implements OnInit {

  movimientos: Movimiento[] = [];
  cuentas: Cuenta[] = [];
  filtro = '';
  errorMsg = '';
  successMsg = '';

  showForm = false;
  editando: Movimiento | null = null;
  form!: FormGroup;

  constructor(private movService: MovimientoService,
              private cuentaService: CuentaService,
              private fb: FormBuilder) {}

  ngOnInit(): void {
    this.buildForm();
    this.cargar();
    this.cuentaService.listar().subscribe(c => this.cuentas = c);
  }

  private buildForm(): void {
    this.form = this.fb.group({
      numeroCuenta: ['', [Validators.required]],
      tipoMovimiento: ['DEPOSITO', [Validators.required]],
      valor: [0, [Validators.required, Validators.min(0.01)]]
    });
  }

  cargar(): void {
    this.movService.listar().subscribe({
      next: (data) => this.movimientos = data,
      error: (err) => this.errorMsg = err.error?.message || 'Error al cargar movimientos'
    });
  }

  get movimientosFiltrados(): Movimiento[] {
    const q = this.filtro.trim().toLowerCase();
    if (!q) return this.movimientos;
    return this.movimientos.filter(m =>
      m.numeroCuenta.toLowerCase().includes(q) ||
      m.tipoMovimiento.toLowerCase().includes(q)
    );
  }

  saldoActualDe(numeroCuenta: string): number {
    const cuenta = this.cuentas.find(c => c.numeroCuenta === numeroCuenta);
    if (!cuenta) return 0;
    const movs = this.movimientos.filter(m => m.numeroCuenta === numeroCuenta);
    if (movs.length === 0) return cuenta.saldoInicial;
    // Tomamos el saldo del ultimo movimiento de la cuenta
    const ultimo = movs[movs.length - 1];
    return ultimo.saldo ?? cuenta.saldoInicial;
  }

  abrirNuevo(): void {
    this.editando = null;
    this.form.reset({ tipoMovimiento: 'DEPOSITO', valor: 0 });
    this.showForm = true;
    this.clearMessages();
  }

  abrirEditar(m: Movimiento): void {
    this.editando = m;
    this.form.patchValue({
      numeroCuenta: m.numeroCuenta,
      tipoMovimiento: m.tipoMovimiento,
      valor: Math.abs(m.valor)
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
    const data: Movimiento = this.form.value;
    const obs = this.editando && this.editando.id
      ? this.movService.actualizar(this.editando.id, data)
      : this.movService.crear(data);

    obs.subscribe({
      next: () => {
        this.successMsg = this.editando ? 'Movimiento actualizado' : 'Movimiento creado';
        this.cerrarForm();
        this.cargar();
      },
      error: (err) => this.errorMsg = err.error?.message || 'Error al guardar'
    });
  }

  eliminar(m: Movimiento): void {
    if (!m.id) return;
    if (!confirm('Eliminar movimiento?')) return;
    this.movService.eliminar(m.id).subscribe({
      next: () => {
        this.successMsg = 'Movimiento eliminado';
        this.cargar();
      },
      error: (err) => this.errorMsg = err.error?.message || 'Error al eliminar'
    });
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
