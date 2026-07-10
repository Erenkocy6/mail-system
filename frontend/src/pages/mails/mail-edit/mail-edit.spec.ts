import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailEdit } from './mail-edit';
import { createComponentTestProviders, mockMail } from '../../../testing/component-test-helpers';

describe('MailEdit', () => {
  let component: MailEdit;
  let fixture: ComponentFixture<MailEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailEdit],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailEdit);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', mockMail.id);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
