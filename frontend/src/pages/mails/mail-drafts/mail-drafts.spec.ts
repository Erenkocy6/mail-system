import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailDrafts } from './mail-drafts';
import { createComponentTestProviders } from '../../../testing/component-test-helpers';

describe('MailDrafts', () => {
  let component: MailDrafts;
  let fixture: ComponentFixture<MailDrafts>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailDrafts],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailDrafts);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
