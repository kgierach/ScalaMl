/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.scalaml.app.chap8

import org.scalaml.workflow.data.DataSource
import org.scalaml.trading.Fundamentals
import org.scalaml.supervised.svm._
import org.scalaml.core.{XTSeries, Types}


import Fundamentals._
import Types.ScalaMl._
object SVCOutliersEval {
   final val path = "resources/data/chap8/dividends2.csv"	
   final val NU = 0.2
   final val GAMMA = 0.5
   final val EPS = 1e-3
   final val NFOLDS = 2
	
	def run: Unit = {
	   Console.println("Evaluation of One class Support Vector Classifier")
	   val extractor = relPriceChange :: 
	                   debtToEquity ::
	                   dividendCoverage ::
	                   cashPerShareToPrice ::
	              //     epsTrend ::
	                   dividendTrend :: List[Array[String] =>Double]()
	   	   
	   DataSource(path, true, false, 1) |> extractor match {
	  	 case Some(xs) => {	  	
	  		 val config = SVMConfig(OneSVCFormulation(NU), RbfKernel(GAMMA), SVMExecution(EPS, NFOLDS))
	  		 val features = XTSeries.transpose(xs.take(xs.size-1))
		     val svc = SVM[Double](config, features, xs.last.map(x => if( x == 0.0) -1.0 else 1.0))
          
		     svc.accuracy match {
	  			 case Some(acc) => println("Accuracy: " + acc)
	  			 case None => println("Could not validate the training set")
	  		 }
	  	 }
	  	 
	  	 case None => println("Dividend data extration failed ")
	   }
	}
}


// --------------------------  EOF -----------------------------------------------