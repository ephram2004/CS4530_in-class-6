package sql.sales;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

import io.micrometer.common.lang.Nullable;
import sqlobjs.ASQLObj;

public class DynamicHomeSale extends ASQLObj {

    public int propertyId;
    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date downloadDate;
    @Nullable
    public String councilName;
    public int purchasePrice;
    @Nullable
    public String address;
    public int postCode;
    @Nullable
    public String propertyType;
    @Nullable
    public Integer strataLotNumber;
    @Nullable
    public String propertyName;
    public double area;
    @Nullable
    public String areaType;
    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date contractDate;
    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date settlementDate;
    @Nullable
    public String zoning;
    @Nullable
    public String natureOfProperty;
    @Nullable
    public String primaryPurpose;
    @Nullable
    public String legalDescription;

    public DynamicHomeSale(JsonNode json) {
        super(json);
    }
}
