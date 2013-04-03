package com.parship.roperty;

import com.parship.commons.util.Ensure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;


/**
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues {

	private static class DomainPattern implements Comparable<DomainPattern> {
		private final Pattern pattern;
		private final int ordering;
		private final Object value;

		public DomainPattern(final String domainPattern, final int order, Object value) {
			Ensure.notNull(domainPattern, "domainPattern");
			String patternStr = domainPattern.replaceAll("\\|", "\\\\|").replaceAll("\\*", "[^|]*");
			patternStr += ".*";
			this.pattern = Pattern.compile(patternStr);
			this.ordering = order;
			this.value = value;
		}

		@Override
		public int compareTo(final DomainPattern other) {
			int order = this.ordering - other.ordering;
			if (order == 0) {
				return pattern.toString().compareTo(other.pattern.toString());
			}
			return order;
		}

		@Override
		public String toString() {
			return "DomainPattern{" +
				"pattern=" + pattern +
				", ordering=" + ordering +
				", value=" + value +
				'}';
		}
	}

	private Map<String, Object> values = new HashMap<>();
	private Set<DomainPattern> patterns = new TreeSet<>();

	public KeyValues() {
		this("[value undefined]");
	}

	public KeyValues(Object value) {
		Ensure.notNull(value, "value");
		values.put("", value);
	}

	public void put(Object value, String... domains) {
		Ensure.notNull(domains, "domain");
		Ensure.notNull(value, "value");
		values.put(buildDomain(value, domains), value);
//		for (String domain : domains) {
//			domainResolvers.put(new DomainResolver());
//		}
//		values.put(domain, value);
	}

	private String buildDomain(final Object value, final String[] domains) {
		int order = 1;
		if (domains.length == 0) {
			addDomainPattern("", order, value);
			return "";
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String domain : domains) {
			i++;
			if (builder.length() > 0) {
				builder.append("|");
			}
//			order <<= order;
			if (!"*".equals(domain)) {
				order = order | (int) Math.pow(2, i);
			}
			builder.append(domain);
		}
		String string = builder.toString();
		addDomainPattern(string, order, value);
		return string;
	}

	private void addDomainPattern(final String pattern, final int order, final Object value) {
		DomainPattern domainPattern = new DomainPattern(pattern, order, value);
		patterns.remove(domainPattern);
		patterns.add(domainPattern);
	}

	private String buildDomain(final Iterable<String> domains, final Resolver resolver) {
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			if (builder.length() > 0) {
				builder.append("|");
			}
			builder.append(resolver.getDomainValue(domain));
		}
		return builder.toString();
	}

	public <T> T get(List<String> domains, final Resolver resolver) {
		T value = (T)values.get("");
		for (DomainPattern pattern : patterns) {
			String domainStr = buildDomain(domains, resolver);
			if (pattern.pattern.matcher(domainStr).matches()) {
				value = (T)pattern.value;
			}
		}
		return value;
//		if (resolver == null) {
//			return value;
//		}
//		StringBuilder builder = new StringBuilder();
//		for (String domain : domains) {
//			if (builder.length() > 0) {
//				builder.append("|");
//			} domain1|domain2|domain3
//			String domainValue = resolver.getDomainValue(domain);
//			Ensure.notEmpty(domainValue, "domainValue");
//			builder.append(domainValue);
//			T overriddenValue = (T)values.get(builder.toString());
//			if (overriddenValue != null) {
//				value = overriddenValue;
//			}
//		}
//		return value;
	}

	@Override
	public String toString() {
		return "KeyValues{" +
			"patterns=" + patterns +
			'}';
	}
}
