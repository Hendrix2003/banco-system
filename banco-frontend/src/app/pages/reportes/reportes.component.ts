import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { ReporteItem } from '../../models/movimiento.model';
import { MovimientoService } from '../../services/movimiento.service';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reportes.component.html'
})
export class ReportesComponent implements OnInit {

  clientes: Cliente[] = [];
  resultados: ReporteItem[] = [];
  loading = false;
  errorMsg = '';
  pdfBase64: string | null = null;

  form!: FormGroup;

  constructor(private movService: MovimientoService,
              private clienteService: ClienteService,
              private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      cliente: ['', [Validators.required]],
      fechaInicio: ['', [Validators.required]],
      fechaFin: ['', [Validators.required]]
    });
    this.clienteService.listar().subscribe(c => this.clientes = c);
  }

  // Consulta el reporte en formato JSON
  consultar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMsg = '';
    this.pdfBase64 = null;
    const { cliente, fechaInicio, fechaFin } = this.form.value;
    this.movService.generarReporte(cliente, fechaInicio, fechaFin, 'json').subscribe({
      next: (res) => {
        this.resultados = res.movimientos || [];
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = err.error?.message || 'Error al generar reporte';
        this.loading = false;
      }
    });
  }

  // Solicita el PDF y lo descarga en el navegador
  descargarPdf(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMsg = '';
    const { cliente, fechaInicio, fechaFin } = this.form.value;
    this.movService.generarReporte(cliente, fechaInicio, fechaFin, 'pdf').subscribe({
      next: (res) => {
        this.resultados = res.movimientos || [];
        if (res.pdfBase64) {
          this.descargarBase64Pdf(res.pdfBase64, `reporte_${cliente}.pdf`);
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = err.error?.message || 'Error al generar PDF';
        this.loading = false;
      }
    });
  }

  // Limpia el formulario y los resultados
  limpiar(): void {
    this.form.reset();
    this.resultados = [];
    this.errorMsg = '';
  }

  // Convierte base64 a blob y dispara la descarga
  private descargarBase64Pdf(base64: string, filename: string): void {
    const byteString = atob(base64);
    const ab = new ArrayBuffer(byteString.length);
    const ia = new Uint8Array(ab);
    for (let i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }
    const blob = new Blob([ab], { type: 'application/pdf' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  esInvalido(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.dirty || c.touched);
  }
}
