import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Movimiento, ReporteResponse } from '../models/movimiento.model';

@Injectable({ providedIn: 'root' })
export class MovimientoService {

  private url = `${environment.apiUrl}/movimientos`;
  private reporteUrl = `${environment.apiUrl}/reportes`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.url);
  }

  obtener(id: number): Observable<Movimiento> {
    return this.http.get<Movimiento>(`${this.url}/${id}`);
  }

  crear(mov: Movimiento): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.url, mov);
  }

  actualizar(id: number, mov: Movimiento): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.url}/${id}`, mov);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  // Genera reporte por cliente y rango de fechas
  generarReporte(cliente: string, fechaInicio: string, fechaFin: string, formato: string): Observable<ReporteResponse> {
    const params = new HttpParams()
      .set('cliente', cliente)
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('formato', formato);
    return this.http.get<ReporteResponse>(this.reporteUrl, { params });
  }
}
