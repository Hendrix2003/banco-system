import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="sidebar">
      <ul>
        <li><a routerLink="/clientes" routerLinkActive="active">Clientes</a></li>
        <li><a routerLink="/cuentas" routerLinkActive="active">Cuentas</a></li>
        <li><a routerLink="/movimientos" routerLinkActive="active">Movimientos</a></li>
        <li><a routerLink="/reportes" routerLinkActive="active">Reportes</a></li>
      </ul>
    </nav>
  `
})
export class SidebarComponent {}
