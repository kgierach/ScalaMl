/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.scalaml.supervised.regression.linear

import org.scalaml.core.{XTSeries, Types}
import Types.ScalaMl._
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.apache.commons.math3.exception.{MathIllegalArgumentException, MathRuntimeException, OutOfRangeException}
import org.scalaml.workflow.PipeOperator
import org.scalaml.core.Types.CommonMath._
import scala.annotation.implicitNotFound
import org.scalaml.supervised.regression.RegressionModel
import scala.util.{Try, Success, Failure}
import org.apache.log4j.Logger
import org.scalaml.util.Display


		/**
		 * <p>Class that defines a Multivariate linear regression. The regression
		 * coefficients or weights are computed during the instantiation of the 
		 * class. The computation of the regression coefficients uses the 
		 * Apache commons Math library. THe regression coefficients are
		 * initialized as None if the training fails.</p>
		 * @param xt input multi-dimensional time series for which regression is to be computed
		 * @param y labeled data for the Multivariate linear regression
		 * @throws IllegalArgumentException if the input time series or the labeled data are undefined or have different sizes
		 * 
		 * @author Patrick Nicolas
		 * @since April 19, 2014
		 * @note Scala for Machine Learning
		 */
@implicitNotFound("Implicit conversion to Double for MultiLinearRegression is missing")
final class MultiLinearRegression[T <% Double](val xt: XTSeries[Array[T]], val labels: DblVector) 
                    extends OLSMultipleLinearRegression with PipeOperator[Array[T], Double] {
	
	require(xt != null && xt.size > 0, "Cannot create perform a Multivariate linear regression on undefined time series")
	require(labels != null && labels.size > 0, "Cannot train a Multivariate linear regression with undefined labels")
    require (xt.size == labels.size, "Size of Input data " + xt.size + " and labels " + labels.size + " for Multivariate linear regression are difference")
		
    type Feature = Array[T]
	private val logger = Logger.getLogger("MultiLinearRegression")
	
	private[this] val model: Option[RegressionModel] = {
	  Try {
		 newSampleData(labels, xt.toDblMatrix)
		 val wRss = (estimateRegressionParameters, calculateResidualSumOfSquares)
	 	 RegressionModel(wRss._1, wRss._2)
	  } match {
	  	case Success(m) => Some(m)
	  	case Failure(e) => Display.error("MultiLinearRegression.model ", logger, e); None
	  }
	}
	
		/**
		 * <p>Retrieve the weight of the multi-variable linear regression
		 * if model has been successfully trained, None otherwise.</p>
		 * @return weights if model is successfully created, None otherwise
		 */
	final def weights: Option[DblVector] = model match {
		case Some(m) => Some(m.weights)
		case None => None
	}
	
		/**
		 * <p>Retrieve the residual sum of squares for this multi-variable linear regression
		 * if model has been successfully trained, None otherwise.</p>
		 * @return residual sum of squares if model is successfully created, None otherwise
		 */
	final def rss: Option[Double] = model match {
		case Some(m) => Some(m.rss)
		case None => None
	}

    
		/**
		 * <p>Data transformation that predicts the value of a vector input.</p>
		 * @param x Array of parameterized values
		 * @throws IllegalStateException if the input array is undefined
		 * @return predicted value if the model has been successfully trained, None otherwise
		 */
	override def |> (x: Feature): Option[Double] = model match {
	   case Some(m) => {
    	 if( x == null || x.size != m.size -1) 
    		 throw new IllegalStateException("Size of input data for prediction " + x.size + " should be " + (m.size -1))
    	 
         Some(x.zip(m.weights.drop(1)).foldLeft(m.weights(0))((s, z) => s + z._1*z._2))
       }
       case None => None
	}
}



		/**
		 * <p>Companion object that defines the 
		 * constructor for the class MultiLinearRegression.</p>
		 * 
		 * @author Patrick Nicolas
		 * @since April 19, 2014
		 */
object MultiLinearRegression {
	def apply[T <% Double](xt: XTSeries[Array[T]], y: DblVector): MultiLinearRegression[T] = new MultiLinearRegression[T](xt, y)
}

// ------------------------------  EOF ---------------------------------------