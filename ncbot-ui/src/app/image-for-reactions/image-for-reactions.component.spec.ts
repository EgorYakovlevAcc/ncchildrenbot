import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageForReactionsComponent } from './image-for-reactions.component';

describe('ImageForReactionsComponent', () => {
  let component: ImageForReactionsComponent;
  let fixture: ComponentFixture<ImageForReactionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImageForReactionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImageForReactionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
