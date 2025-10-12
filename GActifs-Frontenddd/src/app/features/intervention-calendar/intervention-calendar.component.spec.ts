import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InterventionCalendarComponent } from './intervention-calendar.component';

describe('InterventionCalendarComponent', () => {
  let component: InterventionCalendarComponent;
  let fixture: ComponentFixture<InterventionCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterventionCalendarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InterventionCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
