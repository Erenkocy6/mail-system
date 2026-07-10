import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SideNavigation } from './side-navigation';
import { createComponentTestProviders } from '../../../testing/component-test-helpers';

describe('SideNavigation', () => {
  let component: SideNavigation;
  let fixture: ComponentFixture<SideNavigation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideNavigation],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(SideNavigation);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
