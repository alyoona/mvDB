package com.stroganova.movielanddb;

import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Utils {

    static int fill(String sql, List<SqlParameterSource> list, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        SqlParameterSource[] batch = getBatch(list);
        int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(sql, batch);
        return updateCounts.length;
    }

    private static SqlParameterSource[] getBatch(List<SqlParameterSource> list) {
        SqlParameterSource[] batch = new SqlParameterSource[list.size()];
        int i = 0;
        for(SqlParameterSource parameterSource : list) {
            batch[i++] = parameterSource;
        }
        return batch;
    }

    static Map<String, ?> parseLine(String line, String[] columnNames, String delimiters) {
        String[] values = StringUtils.tokenizeToStringArray(line, delimiters, true, true);
        if(columnNames.length != values.length) {
            throw new RuntimeException("parseLine error, columns.length != values.length");
        }
        Map<String, String> parameterSource = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            parameterSource.put(columnNames[i], values[i]);
        }
        return parameterSource;
    }

    static Map<String, Integer> getNameAndIdMap(String sql, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return namedParameterJdbcTemplate.query(sql, EmptySqlParameterSource.INSTANCE
                , resultSet -> {
                    Map<String, Integer> map = new HashMap<>();
                    while (resultSet.next()) {
                        map.put(resultSet.getString("name"), resultSet.getInt("id"));
                    }
                    return map;
                });


    }
}
