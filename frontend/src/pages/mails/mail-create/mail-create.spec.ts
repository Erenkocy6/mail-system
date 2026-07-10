import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailCreate } from './mail-create';
import { createComponentTestProviders } from '../../../testing/component-test-helpers';

describe('MailCreate', () => {
  let component: MailCreate;
  let fixture: ComponentFixture<MailCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailCreate],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
