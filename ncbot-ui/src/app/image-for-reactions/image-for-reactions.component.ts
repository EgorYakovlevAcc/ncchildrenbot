import { Component, OnInit } from '@angular/core';
import {ImageFileService} from "../service/image-file.service";
import {ScoreRangeResult} from "../model/score-range-result";
import {ScoreRangeResultService} from "../service/score-range-result.service";

@Component({
  selector: 'app-image-for-reactions',
  templateUrl: './image-for-reactions.component.html',
  styleUrls: ['./image-for-reactions.component.css']
})
export class ImageForReactionsComponent implements OnInit {
  imageFileOk:File;
  imageFileSad:File;
  constructor(private scoreRangeResultService: ScoreRangeResultService) { }

  ngOnInit(): void {
  }

  imageFileOkSend() {
    this.scoreRangeResultService.uploadImageForReaction(this.imageFileOk, "ok").subscribe(result =>{
        alert("SUCCESS");
      },
      error => {
        alert("ERROR");
      })
  }

  imageFileSadSend() {
    this.scoreRangeResultService.uploadImageForReaction(this.imageFileSad, "sad").subscribe(result =>{
        alert("SUCCESS");
      },
      error => {
        alert("ERROR");
      })
  }

  changeImageFileOk(event) {
    this.imageFileOk = event.target.files[0];
  }

  changeImageFileSad(event) {
    this.imageFileSad = event.target.files[0];
  }
}
