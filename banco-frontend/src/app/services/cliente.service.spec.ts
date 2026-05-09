import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ClienteService } from './cliente.service';
import { Cliente } from '../models/cliente.model';
import { environment } from '../../environments/environment';

describe('ClienteService', () => {
  let service: ClienteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClienteService]
    });
    service = TestBed.inject(ClienteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('listar deberia hacer GET a /clientes', () => {
    const fake: Cliente[] = [
      { id: 1, nombre: 'Juan', identificacion: '123', clienteId: 'jp' }
    ];

    service.listar().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].nombre).toBe('Juan');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/clientes`);
    expect(req.request.method).toBe('GET');
    req.flush(fake);
  });

  it('crear deberia hacer POST con el cliente', () => {
    const cliente: Cliente = { nombre: 'Maria', identificacion: '456', clienteId: 'ml', password: '1234' };

    service.crear(cliente).subscribe(res => {
      expect(res.nombre).toBe('Maria');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/clientes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.clienteId).toBe('ml');
    req.flush({ ...cliente, id: 5 });
  });

  it('eliminar deberia hacer DELETE por id', () => {
    service.eliminar(7).subscribe(() => {});

    const req = httpMock.expectOne(`${environment.apiUrl}/clientes/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
