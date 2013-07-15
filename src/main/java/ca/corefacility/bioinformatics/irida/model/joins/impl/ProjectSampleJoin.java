/*
 * Copyright 2013 Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.corefacility.bioinformatics.irida.model.joins.impl;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Sample;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.envers.Audited;

/**
 *
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
@Entity
@Table(name="project_sample")
@Audited
public class ProjectSampleJoin implements Join<Project,Sample>{

    public ProjectSampleJoin(){
        created = new Date();
    }
    
    public ProjectSampleJoin(Project subject, Sample object){
        this.project=subject;
        this.sample=object;
        created = new Date();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @ManyToOne
    @JoinColumn(name="project_id")
    private Project project;
    
    @ManyToOne
    @JoinColumn(name="sample_id")
    private Sample sample;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    @Override
    public Project getSubject() {
        return project;
    }

    @Override
    public void setSubject(Project subject) {
        this.project=subject;
    }

    @Override
    public Sample getObject() {
        return sample;
    }

    @Override
    public void setObject(Sample object) {
        this.sample=object;
    }

    @Override
    public Date getTimestamp() {
        return created;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.created=timestamp;
    }    
}
