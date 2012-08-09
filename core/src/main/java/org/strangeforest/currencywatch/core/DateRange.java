package org.strangeforest.currencywatch.core;

import java.util.*;

import com.finsoft.util.*;

public class DateRange extends Range<Date> implements Iterable<Date> {

	public DateRange() {
		super();
	}

	public DateRange(Date from, Date to) {
		super(from, to);
	}

	public Collection<Date> dates() {
		return dates(1);
	}

	public Collection<Date> dates(final int step) {
		checkBothBounded();
		return new AbstractCollection<Date>() {
			@Override public Iterator<Date> iterator() {
				return DateRange.this.iterator(step);
			}
			@Override public int size() {
				return 2 + (DateUtil.dayDifference(getFrom(), getTo()) - 1)/step;
			}
		};
	}

	public int size() {
		return isBothBounded() ? dates().size() : -1;
	}

	@Override public Iterator<Date> iterator() {
		return iterator(1);
	}

	public Iterator<Date> iterator(int step) {
		checkBothBounded();
		return new DateIterator(step);
	}

	private void checkBothBounded() {
		if (!isBothBounded())
			throw new IllegalStateException("Date range is not bounded from both sides.");
	}

	private class DateIterator implements Iterator<Date> {

		private Date date;
		private int step;

		public DateIterator(int step) {
			this.step = step;
		}

		@Override public boolean hasNext() {
			return date == null || date.compareTo(getTo()) < 0;
		}

		@Override public Date next() {
			if (date == null)
				date = getFrom();
			else {
				if (date.compareTo(getTo()) >= 0)
					throw new NoSuchElementException();
				Date nextDate = incDate(date, step);
				if (nextDate.compareTo(getTo()) > 0)
					nextDate = getTo();
				date = nextDate;
			}
			return date;
		}

		@Override public void remove() {
			throw new UnsupportedOperationException();
		}

		private Date incDate(Date date, int days) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.add(Calendar.DATE, days);
			return cal.getTime();
		}
	}
}