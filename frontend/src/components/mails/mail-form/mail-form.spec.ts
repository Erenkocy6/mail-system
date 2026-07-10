import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailForm } from './mail-form';
import { createComponentTestProviders } from '../../../testing/component-test-helpers';

describe('MailForm', () => {
  let component: MailForm;
  let fixture: ComponentFixture<MailForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailForm],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
