import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { MovimientoService } from './movimiento.service';
import { environment } from '../../environments/environment';

describe('MovimientoService', () => {
  let service: MovimientoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MovimientoService]
    });
    service = TestBed.inject(MovimientoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('crear movimiento deberia hacer POST', () => {
    service.crear({ numeroCuenta: '123', tipoMovimiento: 'DEPOSITO', valor: 100 })
      .subscribe(res => expect(res.valor).toBe(100));

    const req = httpMock.expectOne(`${environment.apiUrl}/movimientos`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, numeroCuenta: '123', tipoMovimiento: 'DEPOSITO', valor: 100, saldo: 200 });
  });

  it('generarReporte JSON deberia incluir parametros', () => {
    service.generarReporte('jperez', '2024-01-01', '2024-01-31', 'json').subscribe(res => {
      expect(res.formato).toBe('json');
    });

    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/reportes`);
    expect(req.request.params.get('cliente')).toBe('jperez');
    expect(req.request.params.get('fechaInicio')).toBe('2024-01-01');
    expect(req.request.params.get('formato')).toBe('json');
    req.flush({ formato: 'json', movimientos: [] });
  });
});
