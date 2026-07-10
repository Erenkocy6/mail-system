import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RootLayout } from './root-layout';
import { createComponentTestProviders } from '../../testing/component-test-helpers';

describe('RootLayout', () => {
  let component: RootLayout;
  let fixture: ComponentFixture<RootLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RootLayout],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(RootLayout);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
