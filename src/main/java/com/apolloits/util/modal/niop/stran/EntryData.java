package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name="EntryData")
public class EntryData {
	@XmlElement(name = "EntryDateTime")
	public String EntryDateTime;
	@XmlElement(name = "EntryPlaza")
	public String EntryPlaza;
	@XmlElement(name = "EntryPlazaDesc")
	public String EntryPlazaDesc;
	@XmlElement(name = "EntryLane")
	public String EntryLane;

}