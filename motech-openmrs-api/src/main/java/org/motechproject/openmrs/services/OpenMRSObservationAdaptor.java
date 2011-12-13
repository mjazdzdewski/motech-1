package org.motechproject.openmrs.services;

import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.services.MRSObservationAdaptor;
import org.openmrs.*;
import org.openmrs.api.ObsService;
import org.openmrs.logic.result.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class OpenMRSObservationAdaptor implements MRSObservationAdaptor {

    @Autowired
    OpenMRSConceptAdaptor conceptAdaptor;

    @Autowired
    ObsService obsService;

    public <T> Obs createOpenMRSObservationForEncounter(MRSObservation<T> mrsObservation, Encounter encounter, Patient patient, Location facility, User staff) {
        Obs openMrsObservation = new Obs();
        openMrsObservation.setConcept(conceptAdaptor.getConceptByName(mrsObservation.getConceptName()));
        openMrsObservation.setPerson(patient);
        openMrsObservation.setLocation(facility);
        openMrsObservation.setCreator(staff);
        openMrsObservation.setEncounter(encounter);
        openMrsObservation.setObsDatetime(mrsObservation.getDate());
        writeValueToOpenMRSObservation(mrsObservation.getValue(), openMrsObservation);
        return openMrsObservation;
    }

    <T> void writeValueToOpenMRSObservation(T value, Obs openMRSObservation) {
        if (value instanceof Double) {
            openMRSObservation.setValueNumeric((Double) value);
        } else if (value instanceof String) {
            openMRSObservation.setValueText((String) value);
        } else if (value instanceof Boolean) {
            openMRSObservation.setValueNumeric(Boolean.TRUE.equals(value) ? 1.0 : 0.0);
        } else if (value instanceof Date) {
            openMRSObservation.setValueDatetime((Date) value);
        }else{
            throw new IllegalArgumentException("Invalid value of the createMRSObservation-" + value);
        }
    }

    public Set<Obs> createOpenMRSObservationsForEncounter(Set<MRSObservation> mrsObservations, Encounter encounter, Patient patient, Location facility, User staff) {
        Set<Obs> openMrsObservations = new HashSet<Obs>();
        for (MRSObservation observation : mrsObservations) {
            openMrsObservations.add(createOpenMRSObservationForEncounter(observation, encounter, patient, facility, staff));
        }
        return openMrsObservations;
    }

    public Set<MRSObservation> convertOpenMRSToMRSObservations(Set<Obs> openMrsObservations) {
        Set<MRSObservation> mrsObservations = new HashSet<MRSObservation>();
        for (Obs obs : openMrsObservations) {
            mrsObservations.add(convertOpenMRSToMRSObservation(obs));
        }
        return mrsObservations;
    }

    public MRSObservation convertOpenMRSToMRSObservation(Obs obs) {
        switch (new Result(obs).getDatatype()) {
            case BOOLEAN: return createMRSObservation(obs, obs.getValueAsBoolean());
            case DATETIME: return createMRSObservation(obs, obs.getValueDatetime());
            case NUMERIC: return createMRSObservation(obs, obs.getValueNumeric());
            case TEXT: return createMRSObservation(obs, obs.getValueText());
            default: throw new IllegalArgumentException("Invalid value of the createMRSObservation from DB-" + obs);
        }
    }

    private MRSObservation createMRSObservation(Obs obs, Object value) {
        return new MRSObservation(Integer.toString(obs.getId()), obs.getObsDatetime(), obs.getConcept().getName().getName(), value);
    }

    public MRSObservation saveObservation(MRSObservation mrsObservation,Encounter encounter,Patient patient, Location facility, User staff) {
        return convertOpenMRSToMRSObservation(obsService.saveObs(createOpenMRSObservationForEncounter(mrsObservation, encounter, patient, facility, staff), null));
    }
}