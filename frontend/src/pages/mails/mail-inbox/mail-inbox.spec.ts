import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailInbox } from './mail-inbox';
import { createComponentTestProviders } from '../../../testing/component-test-helpers';

describe('MailInbox', () => {
  let component: MailInbox;
  let fixture: ComponentFixture<MailInbox>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailInbox],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailInbox);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
