import { Component, OnInit } from '@angular/core';
import { Employee, EmployeeService } from './services/employee';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {

  employees: Employee[] = [];

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.employeeService.getEmployees().subscribe({
      next: (data) => {
        console.log('Employees received:', data);
        this.employees = data;
      },
      error: (error) => {
        console.error('Error loading employees:', error);
      }
    });
  }
}