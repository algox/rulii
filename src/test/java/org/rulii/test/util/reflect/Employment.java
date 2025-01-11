/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.test.util.reflect;

import org.rulii.model.Identifiable;

import java.math.BigDecimal;

public class Employment implements Identifiable {

    private String id;
    private String companyName;
    private BigDecimal salary;
    private Address address;

    public Employment(String id, String companyName, BigDecimal salary, Address address) {
        super();
        this.id = id;
        this.companyName = companyName;
        this.salary = salary;
        this.address = address;
    }

    @Override
    public String getName() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Employment{" +
                "companyName='" + companyName + '\'' +
                ", salary=" + salary +
                ", address=" + address +
                '}';
    }
}
