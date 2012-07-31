/******************************************************************************/
/* (C) Copyright (and database rights) 1995-2011 Finsoft Ltd., United Kingdom */
/*                                                                            */
/* ALL RIGHTS RESERVED                                                        */
/*                                                                            */
/* The software and information contained herein are proprietary to, and      */
/* comprise valuable trade secrets of, Finsoft Ltd., which intends to         */
/* preserve as trade secrets such software and information. This software     */
/* is furnished pursuant to a written license agreement and may be used,      */
/* copied, transmitted, and stored only in accordance with the terms of       */
/* such license and with the inclusion of the above copyright notice.         */
/* This software and information or any other copies thereof may not be       */
/* provided or otherwise made available to any other person.                  */
/*                                                                            */
/******************************************************************************/

package org.strangeforest.currencywatch.ui;

public interface CurrencyRatePresenterListener {

	void progressChanged(int progress);
	void ratesPerSecChanged(double ratesPerSec);
}
