import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './clientes.component.html'
})
export class ClientesComponent implements OnInit {

  clientes: Cliente[] = [];
  filtro = '';
  loading = false;
  errorMsg = '';
  successMsg = '';

  showForm = false;
  editando: Cliente | null = null;
  form!: FormGroup;

  constructor(private clienteService: ClienteService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.buildForm();
    this.cargar();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      genero: [''],
      edad: [null, [Validators.min(0), Validators.max(120)]],
      identificacion: ['', [Validators.required]],
      direccion: [''],
      telefono: [''],
      clienteId: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(4)]],
      estado: [true]
    });
  }

  cargar(): void {
    this.loading = true;
    this.clienteService.listar().subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = err.error?.message || 'Error al cargar clientes';
        this.loading = false;
      }
    });
  }

  get clientesFiltrados(): Cliente[] {
    const q = this.filtro.trim().toLowerCase();
    if (!q) return this.clientes;
    return this.clientes.filter(c =>
      c.nombre.toLowerCase().includes(q) ||
      c.identificacion.toLowerCase().includes(q) ||
      c.clienteId.toLowerCase().includes(q)
    );
  }

  abrirNuevo(): void {
    this.editando = null;
    this.form.reset({ estado: true });
    this.form.get('password')?.setValidators([Validators.required, Validators.minLength(4)]);
    this.form.get('password')?.updateValueAndValidity();
    this.showForm = true;
    this.clearMessages();
  }

  abrirEditar(c: Cliente): void {
    this.editando = c;
    this.form.patchValue({
      nombre: c.nombre,
      genero: c.genero,
      edad: c.edad,
      identificacion: c.identificacion,
      direccion: c.direccion,
      telefono: c.telefono,
      clienteId: c.clienteId,
      password: '',
      estado: c.estado
    });
    // En edicion el password no es obligatorio si no se cambia
    this.form.get('password')?.clearValidators();
    this.form.get('password')?.updateValueAndValidity();
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
    const data: Cliente = this.form.value;
    // Si no se ingresa password en edicion, no lo enviamos
    if (this.editando && !data.password) {
      delete data.password;
    }

    const obs = this.editando && this.editando.id
      ? this.clienteService.actualizar(this.editando.id, data)
      : this.clienteService.crear(data);

    obs.subscribe({
      next: () => {
        this.successMsg = this.editando ? 'Cliente actualizado' : 'Cliente creado';
        this.cerrarForm();
        this.cargar();
      },
      error: (err) => {
        this.errorMsg = err.error?.message || 'Error al guardar';
      }
    });
  }

  eliminar(c: Cliente): void {
    if (!c.id) return;
    if (!confirm('Eliminar cliente ' + c.nombre + '?')) return;
    this.clienteService.eliminar(c.id).subscribe({
      next: () => {
        this.successMsg = 'Cliente eliminado';
        this.cargar();
      },
      error: (err) => {
        this.errorMsg = err.error?.message || 'Error al eliminar';
      }
    });
  }

  private clearMessages(): void {
    this.errorMsg = '';
    this.successMsg = '';
  }

  // Helpers para el template
  esInvalido(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.dirty || c.touched);
  }
}
