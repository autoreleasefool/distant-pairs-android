//
//  ViewController.swift
//  Partners
//
//  Created by Joseph Roque on 2015-07-09.
//  Copyright (c) 2015 Joseph Roque. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var scrollview: UIScrollView!
    
    var colors:[UIColor] = [UIColor.redColor(), UIColor.blueColor(), UIColor.greenColor(), UIColor.yellowColor()]
    var frame: CGRect = CGRectMake(0, 0, 0, 0)
    
    override func viewDidLoad() {
        super.viewDidLoad()

        for index in 0..<colors.count {
            frame.origin.x = self.scrollview.frame.size.width * CGFloat(index)
            frame.size = self.scrollview.frame.size
            self.scrollview.pagingEnabled = true
            
            var subView = UIView(frame: frame)
            subView.backgroundColor = colors[index]
            self.scrollview.addSubview(subView)
        }
        
        self.scrollview.contentSize = CGSizeMake(self.scrollview.frame.size.width * CGFloat(colors.count), self.scrollview.frame.size.height)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

